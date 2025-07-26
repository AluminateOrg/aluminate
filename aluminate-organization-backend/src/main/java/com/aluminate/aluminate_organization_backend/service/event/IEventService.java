package com.aluminate.aluminate_organization_backend.service.event;

import com.aluminate.aluminate_organization_backend.dto.event.CreateEventRequest;
import com.aluminate.aluminate_organization_backend.dto.event.EventResponseDTO;
import com.aluminate.aluminate_organization_backend.model.Event;

import java.util.List;

public interface IEventService {
    Event createEvent(CreateEventRequest request);
    List<EventResponseDTO> getAllEvents();
    EventResponseDTO publishEvent(Long id);
    EventResponseDTO cancelEvent(Long id);
    EventResponseDTO deleteEvent(Long id);
}
