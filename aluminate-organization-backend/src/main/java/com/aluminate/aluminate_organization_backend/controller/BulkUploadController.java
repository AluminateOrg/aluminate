package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.MemberRowDTO;
import com.aluminate.aluminate_organization_backend.service.AnnotationValidationService;
import com.aluminate.aluminate_organization_backend.service.CsvParserService;
import com.aluminate.aluminate_organization_backend.service.GeminiValidationService;
import com.aluminate.aluminate_organization_backend.service.MemberService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/admin")
public class BulkUploadController {
    @Autowired
    private CsvParserService csvParserService;
    @Autowired
    private AnnotationValidationService annotationValidationService;
    @Autowired
    private GeminiValidationService geminiValidationService;
    @Autowired
    private MemberService memberService;

    @PostMapping("/bulk-upload")
    public ResponseEntity<Map<String, Object>> bulkUpload(@RequestParam("file") MultipartFile file) throws IOException, JsonProcessingException {
        List<MemberRowDTO> parsedRows = csvParserService.parseCSV(file);

        //annotation validation
        List<MemberRowDTO> validated = annotationValidationService.validateRows(parsedRows);

        System.out.println("validated rows by the first phase: " + validated);

        //ai validate for invalid rows
        List<MemberRowDTO> invalidRows = validated.stream().filter(r -> "invalid".equals(r.getStatus())).toList();
        if (!invalidRows.isEmpty()) {
            invalidRows = geminiValidationService.validateWithGemini(invalidRows);
            System.out.println("Gemini validation failed: " + invalidRows);
        }
        //save valid rows
        List<MemberRowDTO> validRows = validated.stream().filter(r -> "valid".equals(r.getStatus())).toList();
        int savedCount = memberService.saveValidMembers(validRows);
        Map<String, Object> response = new HashMap<>();
        response.put("savedCount", savedCount);
        response.put("invalidRows", invalidRows);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk-finalize")
    public ResponseEntity<Map<String, Object>> finalizeBulkUpload(@RequestBody List<MemberRowDTO> correctedRows) throws JsonProcessingException {
        System.out.println("Corrected rows: " + correctedRows);

        // 1. Initial validation
        List<MemberRowDTO> validated = annotationValidationService.validateRows(correctedRows);
        System.out.println("After annotation validation: " + validated);

        // Separate invalid rows
        List<MemberRowDTO> invalidRows = validated.stream()
                .filter(r -> "invalid".equalsIgnoreCase(r.getStatus()))
                .toList();

        // 2. If there are invalid rows, run Gemini validation
        if (!invalidRows.isEmpty()) {
            System.out.println("Invalid rows before Gemini: " + invalidRows);

            // Call Gemini to validate/fix invalid rows
            List<MemberRowDTO> geminiValidated = geminiValidationService.validateWithGemini(invalidRows);

            // Merge Gemini results back into validated list
            Map<String, MemberRowDTO> geminiMap = geminiValidated.stream()
                    .collect(Collectors.toMap(MemberRowDTO::getNic, g -> g));

            for (int i = 0; i < validated.size(); i++) {
                MemberRowDTO row = validated.get(i);
                if (geminiMap.containsKey(row.getNic())) {
                    validated.set(i, geminiMap.get(row.getNic()));
                }
            }

            // Re-check invalid rows after Gemini
            invalidRows = validated.stream()
                    .filter(r -> "invalid".equalsIgnoreCase(r.getStatus()))
                    .toList();

            // If still invalid rows, return them with 400 Bad Request
            if (!invalidRows.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Some rows are still invalid after Gemini validation.");
                response.put("invalidRows", invalidRows);
                System.out.println("Invalid rows after Gemini: " + invalidRows);
                return ResponseEntity.badRequest().body(response);
            }

            System.out.println("All invalid rows corrected after Gemini validation.");
        }

        // 3. Save valid rows
        List<MemberRowDTO> validRows = validated.stream()
                .filter(r -> "valid".equalsIgnoreCase(r.getStatus()))
                .toList();

        int savedCount = memberService.saveValidMembers(validRows);

        // 4. Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("savedCount", savedCount);
        response.put("invalidRows", invalidRows); // Will be empty if all are valid
        System.out.println("Final response: " + response);

        return ResponseEntity.ok(response);
    }

}
