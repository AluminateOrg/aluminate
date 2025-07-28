package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.MemberRequestDTO;
import com.aluminate.aluminate_organization_backend.dto.MemberResponseDTO;
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
@RequestMapping("${api.prefix}/member")
public class MemberController {

    private final IMemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberServiceP;

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

    @GetMapping("/get/all")
    public ResponseEntity<ApiResponse> getAllMembers() {
        try{
            List<MemberResponseDTO> members = memberServiceP.getAllMembers();
            return ResponseEntity.ok(new ApiResponse("Member retrieved successfully!", members));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Failed to retrieve members.", null));
        }
    }
}