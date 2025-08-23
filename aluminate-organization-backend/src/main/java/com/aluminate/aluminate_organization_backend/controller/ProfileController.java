package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.MemberRequestDTO;
import com.aluminate.aluminate_organization_backend.dto.MemberResponseDTO;
import com.aluminate.aluminate_organization_backend.dto.response.ApiResponse;
import com.aluminate.aluminate_organization_backend.service.members.IMemberService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;


@RestController
@RequestMapping("${api.prefix}")
public class ProfileController {

    private final IMemberService memberService;

    public ProfileController(IMemberService memberService) {
        this.memberService = memberService;
    }

    // BOTH MEMBER AND ADMIN (self profile fetch)
    @GetMapping({"/admin/profile", "/member/profile"})
    public ResponseEntity<ApiResponse> getMyProfile() {
        try {
            MemberResponseDTO profile = memberService.getMyProfile();
            // Ensure password is not exposed
            profile.setPassword(null);
            return ResponseEntity.ok(new ApiResponse("Profile retrieved successfully!", profile));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve profile.", null));
        }
    }

    // BOTH MEMBER AND ADMIN (full update - PUT)
    @PutMapping({"/admin/member/profile", "/member/member/profile"})
    public ResponseEntity<ApiResponse> putMyProfile(@Valid @RequestBody MemberRequestDTO request) {
        try {
            MemberResponseDTO updated = memberService.putMyProfile(request);
            updated.setPassword(null);
            return ResponseEntity.ok(new ApiResponse("Profile updated successfully!", updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse(ex.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to update profile.", null));
        }
    }

    // BOTH MEMBER AND ADMIN (partial update - PATCH)
    @PatchMapping({"/admin/member/profile", "/member/member/profile"})
    public ResponseEntity<ApiResponse> patchMyProfile(@RequestBody MemberRequestDTO request) {
        try {
            MemberResponseDTO updated = memberService.patchMyProfile(request);
            updated.setPassword(null);
            return ResponseEntity.ok(new ApiResponse("Profile updated successfully!", updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse(ex.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to update profile.", null));
        }
    }

    // BOTH MEMBER AND ADMIN (set avatar by URL)
    @PostMapping({"/admin/member/profile/avatar-url", "/member/member/profile/avatar-url"})
    public ResponseEntity<ApiResponse> setAvatarUrl(@RequestParam("url") String url) {
        try {
            memberService.setMyAvatarUrl(url);
            MemberResponseDTO profile = memberService.getMyProfile();
            profile.setPassword(null);
            return ResponseEntity.ok(new ApiResponse("Avatar updated!", profile));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse(ex.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to update avatar.", null));
        }
    }
}
