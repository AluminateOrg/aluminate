package com.aluminate.aluminate_organization_backend.dto.group;

import com.aluminate.aluminate_organization_backend.model.GroupCategory;
import lombok.Data;

@Data
public class CreateGroupRequest {
    private String name;
    private String description;
    private int maxMembers;
    private GroupCategory category;
}
