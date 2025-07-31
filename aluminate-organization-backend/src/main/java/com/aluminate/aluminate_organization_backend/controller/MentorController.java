package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.mentor.MentorRequestDTO;
import com.aluminate.aluminate_organization_backend.dto.mentor.MentorResponseDTO;
import com.aluminate.aluminate_organization_backend.service.mentor.MentorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/user/mentor")
public class MentorController {

    @Autowired
    private MentorService mentorService;

    @PostMapping("/apply")
    public ResponseEntity<?> applyAsMentor(@RequestBody MentorRequestDTO request) {
        try {
            MentorResponseDTO responseDTO = mentorService.applyAsMentor(request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Mentor application submitted successfully");
            response.put("mentor", responseDTO);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error applying as mentor: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
