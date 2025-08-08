package com.aluminate.aluminate_organization_backend.dto.group;

import lombok.Data;

@Data
public class GroupJoinRequest {
    private Long memberId;
    private Long groupId;
}
