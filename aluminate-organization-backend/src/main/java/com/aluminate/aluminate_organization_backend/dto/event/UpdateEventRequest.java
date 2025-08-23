package com.aluminate.aluminate_organization_backend.dto.event;

import com.aluminate.aluminate_organization_backend.model.EventType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class UpdateEventRequest {
    private String title;
    private String description;
    private EventType type;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxParticipants;
    private LocalDate registrationDeadline;
    private BigDecimal price;
    private Boolean isPublic;
    private Boolean requiresApproval;
}