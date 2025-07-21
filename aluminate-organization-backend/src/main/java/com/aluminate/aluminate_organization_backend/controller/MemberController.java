package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.MemberRequestDTO;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.response.ApiResponse;
import com.aluminate.aluminate_organization_backend.service.members.IMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/member")
public class MemberController {

    private final IMemberService memberService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> addMember(@RequestBody MemberRequestDTO request) {
        try {
            Member newMember = memberService.createMember(request);
            return ResponseEntity.ok(new ApiResponse("Member added successfully!", newMember));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Failed to add member.", null));
        }
    }

    @GetMapping("/get/count")
    public ResponseEntity<ApiResponse> getMemberCount() {
        try {
            long count = memberService.getMemberCount();
            return ResponseEntity.ok(new ApiResponse("Member count retrieved successfully!", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve member count.", null));
        }
    }

}