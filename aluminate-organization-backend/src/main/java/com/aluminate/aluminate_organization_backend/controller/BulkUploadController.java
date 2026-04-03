package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.BulkFinalizeRequest;
import com.aluminate.aluminate_organization_backend.dto.MemberRowDTO;
import com.aluminate.aluminate_organization_backend.model.Organization;
import com.aluminate.aluminate_organization_backend.repository.OrganizationRepository;
import com.aluminate.aluminate_organization_backend.service.AnnotationValidationService;
import com.aluminate.aluminate_organization_backend.service.CsvParserService;
import com.aluminate.aluminate_organization_backend.service.GeminiValidationService;
import com.aluminate.aluminate_organization_backend.service.MemberService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
    private final CsvParserService csvParserService;
    private final AnnotationValidationService annotationValidationService;
    private final GeminiValidationService geminiValidationService;
    private final MemberService memberService;
    private final OrganizationRepository organizationRepository;
    private final Logger logger = LoggerFactory.getLogger(BulkUploadController.class);

    public BulkUploadController(CsvParserService csvParserService, AnnotationValidationService annotationValidationService,
                                GeminiValidationService geminiValidationService, MemberService memberService, OrganizationRepository organizationRepository
                                ) {
        this.csvParserService = csvParserService;
        this.annotationValidationService = annotationValidationService;
        this.geminiValidationService = geminiValidationService;
        this.memberService = memberService;
        this.organizationRepository = organizationRepository;
    }

    @PostMapping("/member/bulk-upload")
    public ResponseEntity<Map<String, Object>> bulkUpload(@RequestParam("file") MultipartFile file, @RequestParam("groups") String groupsJson, @RequestParam("organizationId") Long organizationId ) throws IOException, JsonProcessingException {
        List<MemberRowDTO> parsedRows = csvParserService.parseCSV(file);

        logger.info("Parsed rows: {}", parsedRows);

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

        //get the organization by id
        if (organizationId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Organization ID is required"));
        }

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + organizationId));

        //ai validate for invalid rows
        List<MemberRowDTO> invalidRows = validated.stream().filter(r -> "invalid".equals(r.getStatus())).toList();
//        if (!invalidRows.isEmpty()) {
//            invalidRows = geminiValidationService.validateWithGemini(invalidRows);
//            System.out.println("Gemini validation failed: " + invalidRows);
//        }
        //save valid rows
        List<MemberRowDTO> validRows = validated.stream().filter(r -> "valid".equals(r.getStatus())).toList();
        int savedCount = memberService.saveValidMembers(validRows, groupId, organization);
        Map<String, Object> response = new HashMap<>();
        response.put("savedCount", savedCount);
        response.put("invalidRows", invalidRows);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/member/bulk-finalize", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> finalizeBulkUploadJson(@RequestBody BulkFinalizeRequest req) {

        logger.info("finalizeBulkUploadJson called with request: {}", req);

        if (req.getOrganizationId() == null) {
            logger.info("get organizationId is null");
            return ResponseEntity.badRequest().body(Map.of("message", "Organization ID is required"));
        }
        if (req.getGroups() == null || req.getGroups().isEmpty()) {
            logger.info("get groups is empty");
            return ResponseEntity.badRequest().body(Map.of("message", "Group ID is required"));
        }

        logger.info("validating rows: {}", req.getRows());
        List<MemberRowDTO> validated = annotationValidationService.validateRows(req.getRows());
        logger.info("validated rows by the first phase: {}", validated);

        List<MemberRowDTO> invalidRows = validated.stream()
                .filter(r -> "invalid".equalsIgnoreCase(r.getStatus()))
                .toList();
        List<MemberRowDTO> validRows = validated.stream()
                .filter(r -> "valid".equalsIgnoreCase(r.getStatus()))
                .toList();

        int savedCount = 0;
        if (!validRows.isEmpty()) {
            Organization organization = organizationRepository.findById(req.getOrganizationId())
                    .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + req.getOrganizationId()));

            String groupIdStr = req.getGroups().get(0);
            long groupId = Long.parseLong(groupIdStr);

            savedCount = memberService.saveValidMembers(validRows, groupId, organization);
        }

        return ResponseEntity.ok(Map.of(
                "savedCount", savedCount,
                "invalidRows", invalidRows
        ));
    }
}
