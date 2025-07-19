package com.aluminate.aluminate_organization_backend.service;

import com.aluminate.aluminate_organization_backend.dto.MemberRowDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiValidationService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";

    public List<MemberRowDTO> validateWithGemini(List<MemberRowDTO> rows) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Create the prompt
            String prompt = """
                Validate these alumni records. Apply rules:
                - NIC: 9 digits + V/v or 12 digits.
                - Name: alphabetic only.
                - Email: valid email format.
                - Phone: +94XXXXXXXXX format.
                Return a JSON array with:
                status: valid|invalid,
                errors: {field: reason},
                suggestions: {field: value}.
                Do NOT include markdown or any extra text.
                Here is the data:
                """ + mapper.writeValueAsString(rows);

            // Build request body
            GeminiRequest requestBody = new GeminiRequest(
                    List.of(new Content(List.of(new Part(prompt))))
            );

            String urlWithKey = GEMINI_API_URL + "?key=" + geminiApiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate();
            String bodyJson = mapper.writeValueAsString(requestBody);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    urlWithKey,
                    new HttpEntity<>(bodyJson, headers),
                    String.class
            );

            JsonNode root = mapper.readTree(response.getBody());
            String modelOutput = root.at("/candidates/0/content/parts/0/text").asText();
            System.out.println("Raw Gemini Output: " + modelOutput);

            // Sanitize output if it contains code blocks or backticks
            modelOutput = modelOutput.replaceAll("```[a-zA-Z]*", "").replace("```", "").trim();

            List<Map<String, Object>> geminiResults =
                    mapper.readValue(modelOutput, new TypeReference<>() {});

            // Merge Gemini results back into rows
            List<MemberRowDTO> finalList = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) {
                MemberRowDTO original = rows.get(i);
                Map<String, Object> gResult = geminiResults.get(i);

                original.setStatus((String) gResult.getOrDefault("status", "valid"));
                original.setErrors((Map<String, String>) gResult.getOrDefault("errors", Map.of()));
                original.setSuggestions((Map<String, String>) gResult.getOrDefault("suggestions", Map.of()));

                finalList.add(original);
            }

            return finalList;
        } catch (Exception e) {
            throw new RuntimeException("Gemini call failed: ", e);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class GeminiRequest {
        private List<Content> contents;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Content {
        private List<Part> parts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Part {
        private String text;
    }
}
