package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.config.ResponseWrapper;
import com.aluminate.aluminate_organization_backend.dto.mentor.*;
import com.aluminate.aluminate_organization_backend.service.mentor.MentorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}")
public class MentorController {


    private final MentorService mentorService;
    private final Logger logger = LoggerFactory.getLogger(MentorController.class);

    public MentorController(MentorService mentorService) {
        this.mentorService = mentorService;
    }

    @PostMapping("/member/mentor/apply")
    public ResponseEntity<?> applyAsMentor(@RequestBody MentorRequestDTO request) {
        try {
            System.out.println("Received mentor application request: " + request);
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
    @GetMapping("/admin/mentor/get-all-unapproved")
    public ResponseEntity<List<MentorApplicationDTO>> getAllUnapprovedMentors() {
        try {
            List<MentorApplicationDTO> mentors = mentorService.getAllUnapprovedMentors();
            return ResponseEntity.ok(mentors);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/admin/mentor/approve")
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

    @GetMapping({"/admin/mentor/get-all-approved", "/member/mentor/get-all-approved"})
    public ResponseEntity<List<MentorApplicationDTO>> getAllApprovedMentors() {
        try {
            List<MentorApplicationDTO> mentors = mentorService.getAllApprovedMentors();
            return ResponseEntity.ok(mentors);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/member/mentor/get-mentor-details/{id}")
    public ResponseEntity<List<MentorApplicationDTO>> getAllApprovedMentorsExceptSelf(@PathVariable Long id) {
        try {
            List<MentorApplicationDTO> mentors = mentorService.getAllApprovedMentorsExceptSelf(id);
            return ResponseEntity.ok(mentors);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/admin/mentor/dis-approve")
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

    @PostMapping("/member/mentor/request-session")
    public ResponseEntity<Map<String, Object>> requestSession(@RequestBody SessionRequestDTO sessionrequestDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean mentorProgram = mentorService.requestSessionWithMentor(sessionrequestDTO);
            response.put("message", "Session requested successfully");
            response.put("success", mentorProgram);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error requesting session: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/respond-session")
    public ResponseEntity<Map<String, Object>> respondToSessionRequest(@RequestBody SessionRespondDTO sessionRespondDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            if ("accept".equals(sessionRespondDTO.getAction())){
                boolean sessionResponse = mentorService.acceptSession(sessionRespondDTO);
                response.put("message", "Session accepted successfully");
                response.put("success", sessionResponse);
                return ResponseEntity.ok(response);
            } else if ("reject".equals(sessionRespondDTO.getAction())) {
                boolean sessionResponse = mentorService.rejectSession(sessionRespondDTO);
                response.put("message", "Session rejected successfully");
                response.put("success", sessionResponse);
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Invalid action");
                return ResponseEntity.status(400).body(response);
            }

        } catch (Exception e) {
            response.put("message", "Error processing session response: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    //get all sessions by mentor side
    @GetMapping("/member/mentor/get-all-sessions/{mentorId}")
    public ResponseEntity<List<MentorSessionDTO>> getAllSessions(@PathVariable Long mentorId) {
        try {
            List<MentorSessionDTO> sessions = mentorService.getAllSessionsByMentor(mentorId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/member/mentor/get-all-sessions-by-user/{userId}")
    public ResponseEntity<List<MentorSessionDTO>> getAllSessionsByUser(@PathVariable Long userId) {
        try {
            List<MentorSessionDTO> sessions = mentorService.getAllSessionsByMember(userId);
            System.out.println("Sessions: " + sessions);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            return ResponseEntity.status(501).body(null);
        }
    }

    @GetMapping("/member/mentor/connect/get-all-members/{id}")
    public ResponseEntity<ResponseWrapper<List<ConnectMemberResponse>>> getAllMembersToConnect(@PathVariable Long id) {
        List<ConnectMemberResponse> members = mentorService.getAllMembers(id);
        ResponseWrapper<List<ConnectMemberResponse>> response = new ResponseWrapper<>(
                true,
                "Members fetched successfully",
                members
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/mentor/is-mentor/{id}")
    public ResponseEntity<ResponseWrapper<Boolean>> isMentor(@PathVariable Long id) {
        logger.info("Checking if member with ID {} is a mentor", id);
        boolean isMentor = mentorService.isMentor(id);
        logger.info("isMentor: {}", isMentor);
        ResponseWrapper<Boolean> response = new ResponseWrapper<>(
                true,
                isMentor ? "Member is a mentor" : "Member is not a mentor",
                isMentor
        );
        return ResponseEntity.ok(response);
    }

    //update the metor acceptance and update session url and date and time
    @PostMapping("/member/mentor/accept-session/{id}")
    public  ResponseEntity<ResponseWrapper<Boolean>> updateSessionDetails(@RequestBody MentorSessionAcceptDTO mentorSessionAcceptDTO, @PathVariable Long id) {
        boolean isUpdated = mentorService.updateMentorSessionDetails(mentorSessionAcceptDTO, id);
        ResponseWrapper<Boolean> response;
        if (isUpdated) {
            response = new ResponseWrapper<>(
                    true,
                    "Session details updated successfully",
                    true
            );
        } else {
            response = new ResponseWrapper<>(
                    false,
                    "Failed to update session details",
                    false
            );
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/mentor/is-paid/{id}")
    public ResponseEntity<ResponseWrapper<Boolean>> updateIsPaid(@PathVariable Long id) {
        try {
            boolean isPaid = mentorService.updateProgramIsPaid(id);
            if (isPaid) {
                return ResponseEntity.ok(new ResponseWrapper<>(true, "Mentor program is paid", true));
            } else {
                return ResponseEntity.ok(new ResponseWrapper<>(true, "Mentor program is not paid", false));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/member/mentor/get-all-sessions-admin/{userId}")
    public ResponseEntity<List<MentorSessionDTO>> getAllSessionsExceptSelf(@PathVariable Long userId) {
        try {
            List<MentorSessionDTO> sessions = mentorService.getAllSessionsExceptSelf(userId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
