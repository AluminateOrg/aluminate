package com.aluminate.aluminate_organization_backend.dto.mentor;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class SessionRespondDTO {
    private Long id;
    private String programUrl;
    private String action;
}
