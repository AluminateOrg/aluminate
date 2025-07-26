package com.aluminate.aluminate_organization_backend.dto.event;

import com.aluminate.aluminate_organization_backend.model.EventStatus;
import com.aluminate.aluminate_organization_backend.model.EventType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class EventResponseDTO {
    private Long id;
    private String title;
    private String description;
    private EventType type;
    private EventStatus status;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int maxParticipants;
    private int currentParticipants;
    private LocalDate registrationDeadline;
    private BigDecimal price;
    private boolean isDeleted;
    private LocalDateTime deletedAt;

}
