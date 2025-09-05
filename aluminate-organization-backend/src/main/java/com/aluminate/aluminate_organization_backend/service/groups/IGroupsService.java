package com.aluminate.aluminate_organization_backend.service.groups;

import com.aluminate.aluminate_organization_backend.dto.group.GroupJoinRequest;
import com.aluminate.aluminate_organization_backend.dto.group.GroupResponseDTO;
import com.aluminate.aluminate_organization_backend.dto.group.PendingRequestDTO;
import com.aluminate.aluminate_organization_backend.model.Groups;
import com.aluminate.aluminate_organization_backend.dto.group.CreateGroupRequest;

import java.util.List;

public interface IGroupsService {
    Groups createGroup(CreateGroupRequest group);
    List<GroupResponseDTO> getAllGroups();
    GroupResponseDTO deactivateGroup(Long id);
    GroupResponseDTO toggleGroupStatus(Long id);
    GroupResponseDTO deleteGroup(Long id);
    GroupResponseDTO joinGroup(GroupJoinRequest request);
    GroupResponseDTO leaveGroup(Long groupId, Long memberId);
    GroupResponseDTO approveJoinRequest(Long groupId, Long memberId);
    GroupResponseDTO rejectJoinRequest(Long groupId, Long memberId);
    List<PendingRequestDTO> getPendingRequests();
    List<GroupResponseDTO> getGroupsByMember(Long memberId);

    long getGroupCount();
}
