package com.aluminate.aluminate_organization_backend.dto.organization;

import com.aluminate.aluminate_organization_backend.model.Admin;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetOrgDTO {
    private Long id;
    private String organizationName;
    private int maxMemberCount;
    private int currentMemberCount;
    private boolean isDeleted;
    private boolean isMembershipFree;
    private Enum status;
}
