package com.aluminate.aluminate_organization_backend.dto.group;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PendingRequestDTO {
    private Long requestId;
    private Long memberId;
    private Long groupId;
    private String memberName;
    private String groupName;
    private LocalDateTime requestDate;
}
