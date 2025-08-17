package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.group.GroupJoinRequest;
import com.aluminate.aluminate_organization_backend.dto.group.GroupResponseDTO;
import com.aluminate.aluminate_organization_backend.dto.group.PendingRequestDTO;
import com.aluminate.aluminate_organization_backend.model.Groups;
import com.aluminate.aluminate_organization_backend.dto.group.CreateGroupRequest;
import com.aluminate.aluminate_organization_backend.dto.response.ApiResponse;
import com.aluminate.aluminate_organization_backend.service.groups.IGroupsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}")
public class GroupController {
    private final IGroupsService groupsService;

    // ADMIN ONLY
    @PostMapping("/admin/group/create")
    public ResponseEntity<ApiResponse> createGroup(@RequestBody CreateGroupRequest group) {
        try {
            Groups newGroup =  groupsService.createGroup(group);
            return ResponseEntity.ok(new ApiResponse("Group created successfully!", newGroup));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

    // BOTH ADMIN AND MEMBER
    @GetMapping({"/admin/group/get/all", "/member/group/get/all", "/group/get/all"})
    public ResponseEntity<ApiResponse> getAllGroups() {
        try {
            List<GroupResponseDTO> groups = groupsService.getAllGroups();
            return ResponseEntity.ok(new ApiResponse("Groups retrieved successfully!", groups));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

    // ADMIN ONLY
    @PutMapping("/admin/group/{id}/deactivate")
    public ResponseEntity<ApiResponse> deactivateGroup(@PathVariable Long id) {
        try {
            GroupResponseDTO deactivatedGroup = groupsService.deactivateGroup(id);
            return ResponseEntity.ok(new ApiResponse("Group deactivated successfully!", deactivatedGroup));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    // ADMIN ONLY
    @PutMapping("/admin/group/{id}/toggle-status")
    public ResponseEntity<ApiResponse> toggleGroupStatus(@PathVariable Long id) {
        try {
            GroupResponseDTO updatedGroup = groupsService.toggleGroupStatus(id);
            String message = updatedGroup.isActive() ?
                    "Group activated successfully!" :
                    "Group deactivated successfully!";
            return ResponseEntity.ok(new ApiResponse(message, updatedGroup));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    // ADMIN ONLY
    @DeleteMapping("/admin/group/{id}/delete")
    public ResponseEntity<ApiResponse> deleteGroup(@PathVariable Long id) {
        try {
            GroupResponseDTO deletedGroup = groupsService.deleteGroup(id);
            return ResponseEntity.ok(new ApiResponse("Group deleted successfully!", deletedGroup));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    // MEMBER ONLY
    @PostMapping("/member/group/join")
    public ResponseEntity<ApiResponse> joinGroup(@RequestBody GroupJoinRequest request) {
        try {
            GroupResponseDTO result = groupsService.joinGroup(request);
            return ResponseEntity.ok(new ApiResponse("Join request processed successfully!", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to process join request", null));
        }
    }

    // ADMIN ONLY
    @PutMapping("/admin/group/{groupId}/approve/{memberId}")
    public ResponseEntity<ApiResponse> approveJoinRequest(
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        try {
            GroupResponseDTO result = groupsService.approveJoinRequest(groupId, memberId);
            return ResponseEntity.ok(new ApiResponse("Join request approved successfully!", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to approve join request", null));
        }
    }

    // ADMIN ONLY
    @PutMapping("/admin/group/{groupId}/reject/{memberId}")
    public ResponseEntity<ApiResponse> rejectJoinRequest(
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        try {
            GroupResponseDTO result = groupsService.rejectJoinRequest(groupId, memberId);
            return ResponseEntity.ok(new ApiResponse("Join request rejected successfully!", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to reject join request", null));
        }
    }

    // MEMBER ONLY
    @DeleteMapping("/member/group/{groupId}/leave/{memberId}")
    public ResponseEntity<ApiResponse> leaveGroup(
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        try {
            GroupResponseDTO result = groupsService.leaveGroup(groupId, memberId);
            return ResponseEntity.ok(new ApiResponse("Left group successfully!", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to leave group", null));
        }
    }

    // ADMIN ONLY
    @GetMapping("/admin/group/get/pending-requests")
    public ResponseEntity<ApiResponse> getPendingRequests() {
        try {
            List<PendingRequestDTO> pendingRequests = groupsService.getPendingRequests();
            return ResponseEntity.ok(new ApiResponse("Pending requests retrieved successfully!", pendingRequests));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve pending requests", null));
        }
    }

}