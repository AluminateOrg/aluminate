package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.MemberRowDTO;
import com.aluminate.aluminate_organization_backend.service.AnnotationValidationService;
import com.aluminate.aluminate_organization_backend.service.CsvParserService;
import com.aluminate.aluminate_organization_backend.service.GeminiValidationService;
import com.aluminate.aluminate_organization_backend.service.MemberService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public ResponseEntity<Map<String, Object>> bulkUpload(@RequestParam("file") MultipartFile file, @RequestParam("groups") String groupsJson) throws IOException, JsonProcessingException {
        List<MemberRowDTO> parsedRows = csvParserService.parseCSV(file);

        System.out.println("Parsed rows: " + parsedRows);
        ObjectMapper objectMapper = new ObjectMapper();
        int groupId;
        try{
            List<String> groups = objectMapper.readValue(groupsJson, new TypeReference<List<String>>() {});
            if (groups.isEmpty()){
                return ResponseEntity.badRequest().body(Map.of("message", "No groups provided"));
            }
            groupId = Integer.parseInt(groups.get(0));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid group ID format"));
        }
        System.out.println("Group ID: " + groupId);
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
        int savedCount = memberService.saveValidMembers(validRows, groupId);
        Map<String, Object> response = new HashMap<>();
        response.put("savedCount", savedCount);
        response.put("invalidRows", invalidRows);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk-finalize")
    public ResponseEntity<Map<String, Object>> finalizeBulkUpload(@RequestBody Map<String, Object> requestBody) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        // ✅ 1. Extract and parse the rows
        List<MemberRowDTO> correctedRows = mapper.convertValue(requestBody.get("rows"), new TypeReference<List<MemberRowDTO>>() {});
        System.out.println("Corrected rows: " + correctedRows);

        // ✅ 2. Extract group ID
        List<String> groups = mapper.convertValue(requestBody.get("groups"), new TypeReference<List<String>>() {});
        if (groups.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Group ID is required"));
        }
        String groupId = groups.get(0); // Single group ID
        System.out.println("Finalizing for group ID: " + groupId);

        // ✅ 3. Validate rows
        List<MemberRowDTO> validated = annotationValidationService.validateRows(correctedRows);
        System.out.println("After annotation validation: " + validated);

        // ✅ 4. Gemini validation if needed
        List<MemberRowDTO> invalidRows = validated.stream()
                .filter(r -> "invalid".equalsIgnoreCase(r.getStatus()))
                .toList();

        if (!invalidRows.isEmpty()) {
            List<MemberRowDTO> geminiValidated = geminiValidationService.validateWithGemini(invalidRows);
            Map<String, MemberRowDTO> geminiMap = geminiValidated.stream()
                    .collect(Collectors.toMap(MemberRowDTO::getNic, g -> g));

            for (int i = 0; i < validated.size(); i++) {
                MemberRowDTO row = validated.get(i);
                if (geminiMap.containsKey(row.getNic())) {
                    validated.set(i, geminiMap.get(row.getNic()));
                }
            }

            invalidRows = validated.stream()
                    .filter(r -> "invalid".equalsIgnoreCase(r.getStatus()))
                    .toList();

            if (!invalidRows.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Some rows are still invalid after Gemini validation.",
                        "invalidRows", invalidRows
                ));
            }
        }

        // ✅ 5. Save valid members and assign to group
        List<MemberRowDTO> validRows = validated.stream()
                .filter(r -> "valid".equalsIgnoreCase(r.getStatus()))
                .toList();

        int savedCount = memberService.saveValidMembers(validRows, Long.parseLong(groupId));

        return ResponseEntity.ok(Map.of(
                "savedCount", savedCount,
                "invalidRows", invalidRows
        ));
    }

}
