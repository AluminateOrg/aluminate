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

import java.util.*;
import java.util.regex.Pattern;

@Service
public class GeminiValidationService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";

    // List of inappropriate words to filter
    private static final List<String> BAD_WORDS = List.of(
            "xxx", "badword1", "badword2", "fuck", "shit" // Add more as needed
    );

    // Regex patterns for fallback validation
    private static final Pattern NIC_PATTERN = Pattern.compile("^\\d{9}[Vv]$|^\\d{12}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+94\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{IsAlphabetic}\\u0D80-\\u0DFF ]+$");

    public List<MemberRowDTO> validateWithGemini(List<MemberRowDTO> rows) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Enhanced prompt
            String prompt = """
                You are a strict data validator for alumni records.
                Validate and sanitize these records according to these rules:
                1. NIC: Must be 9 digits + V/v OR 12 digits.
                2. Name: Can include Sinhala (Unicode U+0D80–U+0DFF) or English letters only (no numbers or symbols).
                3. Email: Must be a valid email format.
                4. Phone: Must match +94XXXXXXXXX format.
                5. Detect swapped or misplaced values (e.g., phone number in name field).
                6. Detect inappropriate or offensive content in any field.
                7. Provide descriptive corrections and suggestions if the data is invalid.
                
                Return a JSON array, where each object has:
                - status: valid|invalid
                - errors: {field: reason}
                - suggestions: {field: corrected value, explanation: why}
                
                Here is the data to validate:
                """ + mapper.writeValueAsString(rows);

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

            modelOutput = modelOutput.replaceAll("```[a-zA-Z]*", "").replace("```", "").trim();

            List<Map<String, Object>> geminiResults =
                    mapper.readValue(modelOutput, new TypeReference<>() {});

            // Merge Gemini results + fallback validation
            List<MemberRowDTO> finalList = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) {
                MemberRowDTO original = rows.get(i);
                Map<String, Object> gResult = geminiResults.get(i);

                Map<String, String> errors = castToMap(gResult.get("errors"));
                Map<String, String> suggestions = castToMap(gResult.get("suggestions"));

                applyFallbackValidation(original, errors, suggestions);

                original.setStatus(errors.isEmpty() ? "valid" : "invalid");
                original.setErrors(errors);
                original.setSuggestions(suggestions);

                finalList.add(original);
            }

            return finalList;
        } catch (Exception e) {
            throw new RuntimeException("Gemini call failed: ", e);
        }
    }

    /** Fallback validation + field swap detection */
    private void applyFallbackValidation(MemberRowDTO row, Map<String, String> errors, Map<String, String> suggestions) {
        // NIC check
        if (!NIC_PATTERN.matcher(row.getNic()).matches()) {
            errors.put("nic", "Invalid NIC format");
        }

        // Phone check
        if (!PHONE_PATTERN.matcher(row.getPhone()).matches()) {
            errors.put("phone", "Invalid phone number format");
            suggestions.put("phone", "+94XXXXXXXXX");
        }

        // Email check
        if (!EMAIL_PATTERN.matcher(row.getEmail()).matches()) {
            errors.put("email", "Invalid email format");
            suggestions.put("email", "example@gmail.com");
        }

        // Name check
        if (!NAME_PATTERN.matcher(row.getName()).matches()) {
            errors.put("name", "Name must only contain Sinhala or English letters");
        }

        // Bad word check
        if (containsBadWords(row.getName())) {
            errors.put("name", "Contains inappropriate words");
        }
        if (containsBadWords(row.getEmail())) {
            errors.put("email", "Contains inappropriate words");
        }

        // Swap detection: If name looks like phone or NIC
        if (isPhone(row.getName()) && isName(row.getPhone())) {
            errors.put("name", "Field might be swapped with phone");
            errors.put("phone", "Field might be swapped with name");
            suggestions.put("name", row.getPhone());
            suggestions.put("phone", row.getName());
            suggestions.put("explanation", "Swapped 'name' and 'phone' because they were reversed");
        } else if (isNic(row.getName()) && isName(row.getNic())) {
            errors.put("name", "Field might be swapped with NIC");
            errors.put("nic", "Field might be swapped with name");
            suggestions.put("name", row.getNic());
            suggestions.put("nic", row.getName());
            suggestions.put("explanation", "Swapped 'name' and 'nic' because they were reversed");
        }
    }

    private boolean containsBadWords(String input) {
        if (input == null) return false;
        return BAD_WORDS.stream().anyMatch(input.toLowerCase()::contains);
    }

    private boolean isPhone(String value) {
        return value != null && PHONE_PATTERN.matcher(value).matches();
    }

    private boolean isNic(String value) {
        return value != null && NIC_PATTERN.matcher(value).matches();
    }

    private boolean isName(String value) {
        return value != null && NAME_PATTERN.matcher(value).matches();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> castToMap(Object obj) {
        return (obj instanceof Map) ? (Map<String, String>) obj : new HashMap<>();
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
