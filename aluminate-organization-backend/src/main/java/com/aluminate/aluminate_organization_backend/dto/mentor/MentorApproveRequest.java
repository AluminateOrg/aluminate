package com.aluminate.aluminate_organization_backend.dto.mentor;

import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class MentorApproveRequest {
    private Long applicationId;
    private String action; // "approve" or "reject"
}
