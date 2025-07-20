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

@RestController
@RequestMapping("/api/admin")
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

        //ai validate for invalid rows
        List<MemberRowDTO> invalidRows = validated.stream().filter(r -> "invalid".equals(r.getStatus())).toList();
        if (!invalidRows.isEmpty()) {
            invalidRows = geminiValidationService.validateWithGemini(invalidRows);
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
        List<MemberRowDTO> validated = annotationValidationService.validateRows(correctedRows);
        System.out.println("Validated rows: " + validated);
        List<MemberRowDTO> invalidRows = validated.stream().filter(r -> "invalid".equals(r.getStatus())).toList();

        List<MemberRowDTO> validRows = validated.stream().filter(r -> "valid".equals(r.getStatus())).toList();
        int savedCount = memberService.saveValidMembers(validRows);
        Map<String, Object> response = new HashMap<>();
        response.put("savedCount", savedCount);
        response.put("invalidRows", invalidRows);
        return ResponseEntity.ok(response);
    }
}
