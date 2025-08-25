package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.MemberRequestDTO;
import com.aluminate.aluminate_organization_backend.dto.MemberResponseDTO;
import com.aluminate.aluminate_organization_backend.dto.response.ApiResponse;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.service.members.IMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestController
@RequestMapping("${api.prefix}")
public class ProfileController {

    private final IMemberService memberService;

    public ProfileController(IMemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping({"/admin/profile", "/member/profile"})
    public ResponseEntity<ApiResponse> getMyProfile() {
        log.info("GET /profile invoked");
        try {
            MemberResponseDTO profile = memberService.getMyProfile();
            profile.setPassword(null);
            return ResponseEntity.ok(new ApiResponse("Profile retrieved successfully!", profile));
        } catch (ResourceNotFoundException rnfe) {
            log.warn("Profile not found: {}", rnfe.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(rnfe.getMessage(), null));
        } catch (Exception e) {
            log.error("getMyProfile failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve profile.", null));
        }
    }

    // BOTH MEMBER AND ADMIN (full update - PUT)
    // Matches axiosMember PUT("/profile")
    @PutMapping({"/member/profile", "/admin/profile"})
    public ResponseEntity<ApiResponse> putMyProfile(@Valid @RequestBody MemberRequestDTO request) {
        try {
            MemberResponseDTO updated = memberService.putMyProfile(request);
            updated.setPassword(null);
            return ResponseEntity.ok(new ApiResponse("Profile updated successfully!", updated));
        } catch (IllegalArgumentException ex) {
            log.warn("PUT /profile validation error: {}", ex.getMessage());
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse(ex.getMessage(), null));
        } catch (Exception e) {
            log.error("PUT /profile failed", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to update profile.", null));
        }
    }

    // BOTH MEMBER AND ADMIN (partial update - PATCH)
    // Matches axiosMember PATCH("/profile")
    @PatchMapping({"/member/profile", "/admin/profile"})
    public ResponseEntity<ApiResponse> patchMyProfile(@RequestBody MemberRequestDTO request) {
        try {
            MemberResponseDTO updated = memberService.patchMyProfile(request);
            updated.setPassword(null);
            return ResponseEntity.ok(new ApiResponse("Profile updated successfully!", updated));
        } catch (IllegalArgumentException ex) {
            log.warn("PATCH /profile validation error: {}", ex.getMessage());
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse(ex.getMessage(), null));
        } catch (Exception e) {
            log.error("PATCH /profile failed", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to update profile.", null));
        }
    }

    // BOTH MEMBER AND ADMIN (set avatar by URL)
    // Matches axiosMember POST("/profile/avatar-url?url=...")
    @PostMapping({"/member/profile/avatar-url", "/admin/profile/avatar-url"})
    public ResponseEntity<ApiResponse> setAvatarUrl(@RequestParam("url") String url) {
        try {
            memberService.setMyAvatarUrl(url);
            MemberResponseDTO profile = memberService.getMyProfile();
            profile.setPassword(null);
            return ResponseEntity.ok(new ApiResponse("Avatar updated!", profile));
        } catch (IllegalArgumentException ex) {
            log.warn("POST /profile/avatar-url bad request: {}", ex.getMessage());
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse(ex.getMessage(), null));
        } catch (Exception e) {
            log.error("POST /profile/avatar-url failed", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to update avatar.", null));
        }
    }

    // inside ProfileController (it already has @RequestMapping("${api.prefix}"))
// keep the existing imports; add PublicMemberProfileDTO import if needed

    // PUBLIC READ (no auth) — used by QR scans
    @GetMapping("/public/profile/{slug}")
    public ResponseEntity<ApiResponse> getPublicProfileBySlug(@PathVariable String slug) {
        log.info("GET /public/profile/{} invoked", slug);
        try {
            var dto = memberService.getPublicProfileBySlug(slug); // sanitized DTO
            return ResponseEntity.ok(new ApiResponse("OK", dto));
        } catch (ResourceNotFoundException rnfe) {
            log.warn("Public profile not found: {}", rnfe.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(rnfe.getMessage(), null));
        } catch (Exception e) {
            log.error("getPublicProfileBySlug failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to load public profile.", null));
        }
    }

    // MEMBER (auth) — enable or regenerate the share link (slug)
    @PostMapping({"/member/profile/share-link", "/admin/profile/share-link"})
    public ResponseEntity<ApiResponse> enableOrRegenerateShareLink(
            @RequestParam(name = "regenerate", defaultValue = "false") boolean regenerate) {
        try {
            var dto = regenerate ? memberService.regenerateShareLink()
                    : memberService.enableShareLink();
            return ResponseEntity.ok(new ApiResponse("Share link ready", dto));
        } catch (Exception e) {
            log.error("share-link failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to setup share link.", null));
        }
    }

    // MEMBER (auth) — disable the share link
    @DeleteMapping({"/member/profile/share-link", "/admin/profile/share-link"})
    public ResponseEntity<ApiResponse> disableShareLink() {
        try {
            memberService.disableShareLink();
            return ResponseEntity.ok(new ApiResponse("Share link disabled", null));
        } catch (Exception e) {
            log.error("disable share-link failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to disable share link.", null));
        }
    }

}
