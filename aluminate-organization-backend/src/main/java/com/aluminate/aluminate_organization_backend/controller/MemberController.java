package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.MemberRequestDTO;
import com.aluminate.aluminate_organization_backend.dto.MemberResponseDTO;
import com.aluminate.aluminate_organization_backend.dto.group.GroupMembershipStatusDTO;
import com.aluminate.aluminate_organization_backend.dto.response.ApiResponse;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import com.aluminate.aluminate_organization_backend.service.MemberService;
import com.aluminate.aluminate_organization_backend.service.members.IMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}")
public class MemberController {

    private final IMemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberServiceP;

    // ADMIN ONLY
    @PostMapping("/admin/member/create")
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

    // BOTH MEMBER AND ADMIN
    @GetMapping({"/admin/member/get/count", "/member/member/get/count"})
    public ResponseEntity<ApiResponse> getMemberCount() {
        try {
            long count = memberService.getMemberCount();
            return ResponseEntity.ok(new ApiResponse("Member count retrieved successfully!", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve member count.", null));
        }
    }

    // BOTH MEMBER AND ADMIN
    @GetMapping({"/admin/member/get/all", "/member/member/get/all"})
    public ResponseEntity<ApiResponse> getAllMembers() {
        try{
            List<MemberResponseDTO> members = memberServiceP.getAllMembers();
            return ResponseEntity.ok(new ApiResponse("Member retrieved successfully!", members));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Failed to retrieve members.", null));
        }
    }

    // BOTH MEMBER AND ADMIN
    @GetMapping({"/admin/member/{memberId}/groups/membership-status", "/member/member/{memberId}/groups/membership-status"})
    public ResponseEntity<ApiResponse> getMemberGroupMembershipStatuses(@PathVariable Long memberId) {
        try {
            List<GroupMembershipStatusDTO> statuses = memberService.getMemberGroupMembershipStatuses(memberId);
            return ResponseEntity.ok(new ApiResponse("Membership statuses retrieved successfully!", statuses));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve membership statuses", null));
        }
    }
}