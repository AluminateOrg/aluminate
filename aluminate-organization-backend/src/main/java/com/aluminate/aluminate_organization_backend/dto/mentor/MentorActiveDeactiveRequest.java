package com.aluminate.aluminate_organization_backend.dto.mentor;

import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class MentorActiveDeactiveRequest {
    private Long mentorId;
    private String newStatus;
}
