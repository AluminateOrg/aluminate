package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.mentor.*;
import com.aluminate.aluminate_organization_backend.model.Mentor;
import com.aluminate.aluminate_organization_backend.service.mentor.MentorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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

    //get all mentors request
    @GetMapping("/get-all-unapproved")
    public ResponseEntity<List<MentorApplicationDTO>> getAllUnapprovedMentors() {
        try {
            List<MentorApplicationDTO> mentors = mentorService.getAllUnapprovedMentors();
            return ResponseEntity.ok(mentors);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/approve")
    public ResponseEntity<Map<String, Object>> approveMentor(@RequestBody MentorApproveRequest request) {
        try {
            if ("approve".equals(request.getAction())) {
                boolean ApprovedMentor = mentorService.approveMentorApplication(request.getApplicationId());
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Mentor application approved successfully");
                response.put("success", ApprovedMentor);
                return ResponseEntity.ok(response);
            } else if ("reject".equals(request.getAction())) {
                boolean mentor = mentorService.rejectMentorApplication(request.getApplicationId());
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Mentor application rejected successfully");
                response.put("success", mentor);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(500).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/get-all-approved")
    public ResponseEntity<List<MentorApplicationDTO>> getAllApprovedMentors() {
        try {
            List<MentorApplicationDTO> mentors = mentorService.getAllApprovedMentors();
            return ResponseEntity.ok(mentors);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/dis-approve")
    public ResponseEntity<Map<String, Object>> disApproveMentor(@RequestBody MentorActiveDeactiveRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            if ("inactive".equals(request.getNewStatus())){
                boolean disApprovedMentor = mentorService.deactivateMentor(request.getMentorId());
                System.out.println("Disapproved mentor: " + disApprovedMentor);
                if (!disApprovedMentor) {
                    response.put("message", "Mentor application is already deactivated");
                    response.put("success", false);
                    return ResponseEntity.status(400).body(response);
                } else {
                    response.put("message", "Mentor application deactivated successfully");
                    response.put("success", true);
                    return ResponseEntity.ok(response);
                }
            } else if ("active".equals(request.getNewStatus())){
                boolean approveMentor = mentorService.approveMentorApplication(request.getMentorId());
                if (!approveMentor) {
                    response.put("message", "Mentor application is already active");
                    response.put("success", false);
                    return ResponseEntity.status(400).body(response);
                } else {
                    response.put("message", "Mentor application activated successfully");
                    response.put("success", true);
                    return ResponseEntity.ok(response);
                }
            } else {
                response.put("message", "Invalid action");
                return ResponseEntity.status(400).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error deactivating mentor: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
}
