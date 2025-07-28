package com.aluminate.aluminate_organization_backend.dto.event;

import com.aluminate.aluminate_organization_backend.model.EventType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateEventRequest {
    private String title;
    private String description;
    private EventType type;
    private String location;
    private LocalDate startDate;
    private LocalTime startTime;
    private LocalDate endDate;
    private LocalTime endTime;
    private int maxParticipants;
    private LocalDate registrationDeadline;
    private BigDecimal price;
    @JsonProperty("public")
    private boolean isPublic;
    private boolean requiresApproval;
}
