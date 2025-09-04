package com.aluminate.aluminate_organization_backend.chat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.aluminate.aluminate_organization_backend.chat.dto.MessageResponse;
import com.aluminate.aluminate_organization_backend.chat.dto.PageResponse;
import com.aluminate.aluminate_organization_backend.chat.dto.SendMessageRequest;
import com.aluminate.aluminate_organization_backend.chat.security.JwtPrincipal;
import com.aluminate.aluminate_organization_backend.chat.security.JwtService;
import com.aluminate.aluminate_organization_backend.chat.service.ChatService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class ChatRestController {

    private final ChatService chatService;
    private final JwtService jwtService;

    @GetMapping("/orgs/{orgId}/groups/{groupId}/messages")
    public ResponseEntity<PageResponse<MessageResponse>> getMessages(
            @PathVariable String orgId,
            @PathVariable String groupId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String cursor,
            HttpServletRequest http) {

        JwtPrincipal principal = jwtService.validateAndExtract(http.getHeader("Authorization"));
        Assert.isTrue(orgId.equals(principal.getOrgId()), "orgId mismatch");

        return ResponseEntity.ok(chatService.getHistory(orgId, groupId, limit, cursor));
    }

    @PostMapping("/orgs/{orgId}/groups/{groupId}/messages")
    public ResponseEntity<MessageResponse> postMessage(
            @PathVariable String orgId,
            @PathVariable String groupId,
            @Valid @RequestBody SendMessageRequest body,
            HttpServletRequest http) {

        JwtPrincipal principal = jwtService.validateAndExtract(http.getHeader("Authorization"));
        Assert.isTrue(orgId.equals(principal.getOrgId()), "orgId mismatch");

        MessageResponse saved = chatService.sendMessage(orgId, groupId, body, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/orgs/{orgId}/chats/universal/messages")
    public ResponseEntity<PageResponse<MessageResponse>> getUniversal(
            @PathVariable String orgId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String cursor,
            HttpServletRequest http) {

        JwtPrincipal principal = jwtService.validateAndExtract(http.getHeader("Authorization"));
        Assert.isTrue(orgId.equals(principal.getOrgId()), "orgId mismatch");

        return ResponseEntity.ok(chatService.getHistory(orgId, "UNIVERSAL", limit, cursor));
    }
}
