package com.aluminate.aluminate_organization_backend.dto.group;

import com.aluminate.aluminate_organization_backend.model.GroupCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class GroupResponseDTO {
    private Long id;
    private String name;
    private String description;
    private int maxMembers;
    private int currentMembers;
    private LocalDate createdDate;
    private boolean isActive;
    private boolean isDeleted;
    private LocalDateTime deletedAt;
    private boolean requiredApproval;
    private GroupCategory category;
}
