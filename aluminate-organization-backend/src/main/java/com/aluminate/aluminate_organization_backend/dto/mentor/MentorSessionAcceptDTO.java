package com.aluminate.aluminate_organization_backend.dto.mentor;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class MentorSessionAcceptDTO {
    private Long id;
    private String programUrl;
    private LocalDate date;
    private LocalTime time;
}
