package com.aluminate.aluminate_organization_backend.dto.mentor;

import lombok.Data;
import lombok.Getter;

import java.util.Set;

@Data
@Getter
public class SessionRequestDTO {
    private Long mentorId;
    private Set<Long> userId;
    private Long createdBy;
}
