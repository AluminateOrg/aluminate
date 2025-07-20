package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.GroupResponseDTO;
import com.aluminate.aluminate_organization_backend.model.Groups;
import com.aluminate.aluminate_organization_backend.request.CreateGroupRequest;
import com.aluminate.aluminate_organization_backend.response.ApiResponse;
import com.aluminate.aluminate_organization_backend.service.groups.IGroupsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/group")
public class GroupController {
    private final IGroupsService groupsService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createGroup(@RequestBody CreateGroupRequest group) {
        try {
            Groups newGroup =  groupsService.createGroup(group);
            return ResponseEntity.ok(new ApiResponse("Group created successfully!", newGroup));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/get/all")
    public ResponseEntity<ApiResponse> getAllGroups() {
        try {
            List<GroupResponseDTO> groups = groupsService.getAllGroups();
            return ResponseEntity.ok(new ApiResponse("Groups retrieved successfully!", groups));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/{id}/deactivate")
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

    @PutMapping("/{id}/toggle-status")
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

    @DeleteMapping("/{id}/delete")
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

}
