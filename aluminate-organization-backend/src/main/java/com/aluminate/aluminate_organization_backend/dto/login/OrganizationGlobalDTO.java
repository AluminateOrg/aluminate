package com.aluminate.aluminate_organization_backend.dto.login;

import com.aluminate.aluminate_organization_backend.model.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
public class OrganizationGlobalDTO {
    private Long id;
    private String organizationName;
    private int maxMemberCount;
    private int currentMemberCount;
    private Status status;
    private boolean isMembershipFree;
    private boolean isDeleted;
}
