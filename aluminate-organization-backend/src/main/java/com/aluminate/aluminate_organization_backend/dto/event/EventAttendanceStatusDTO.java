package com.aluminate.aluminate_organization_backend.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventAttendanceStatusDTO {
    private Long eventId;
    private boolean attending;
}