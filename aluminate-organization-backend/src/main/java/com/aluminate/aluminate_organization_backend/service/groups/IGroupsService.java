package com.aluminate.aluminate_organization_backend.service.groups;

import com.aluminate.aluminate_organization_backend.dto.GroupResponseDTO;
import com.aluminate.aluminate_organization_backend.model.Groups;
import com.aluminate.aluminate_organization_backend.request.CreateGroupRequest;

import java.util.List;

public interface IGroupsService {
    Groups createGroup(CreateGroupRequest group);
    List<GroupResponseDTO> getAllGroups();
    GroupResponseDTO deactivateGroup(Long id);
    GroupResponseDTO toggleGroupStatus(Long id);
    GroupResponseDTO deleteGroup(Long id);
}
