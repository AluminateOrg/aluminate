package com.aluminate.aluminate_organization_backend.service.event;

import com.aluminate.aluminate_organization_backend.dto.event.*;
import com.aluminate.aluminate_organization_backend.model.Event;

import java.util.List;

public interface IEventService {
    Event createEvent(CreateEventRequest request);
    List<EventResponseDTO> getAllEvents();
    EventResponseDTO publishEvent(Long id);
    EventResponseDTO cancelEvent(Long id);
    EventResponseDTO deleteEvent(Long id);
    EventResponseDTO attendEvent(Long eventId, Long memberId);
    EventResponseDTO unattendEvent(Long eventId, Long memberId);

    EventResponseDTO updateEvent(Long id, UpdateEventRequest request);

    List<MemberAttendanceDTO> getEventAttendance(Long eventId);
    List<EventResponseDTO> getMemberAttendances(Long memberId);

    List<EventAttendanceStatusDTO> getMemberEventAttendanceStatuses(Long memberId);
    byte[] exportEventAttendeesCsv(Long eventId);


}
