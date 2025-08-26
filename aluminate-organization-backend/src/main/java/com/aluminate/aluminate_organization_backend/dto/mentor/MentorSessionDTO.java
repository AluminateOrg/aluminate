package com.aluminate.aluminate_organization_backend.dto.mentor;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class MentorSessionDTO {
    private Long id;
    private String programUrl;
//    private String action;
    private String mentorName;
    private String menteeName;
    private String status;
    private LocalDate date;
    private LocalTime time;
    private String sessionDuration;
//    private String feedback;
    private LocalDateTime createdAt;
    private String menteeEmail;
}
