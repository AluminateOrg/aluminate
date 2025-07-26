package com.aluminate.aluminate_organization_backend.service.event;

import com.aluminate.aluminate_organization_backend.dto.event.CreateEventRequest;
import com.aluminate.aluminate_organization_backend.dto.event.EventResponseDTO;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.model.Event;
import com.aluminate.aluminate_organization_backend.model.EventStatus;
import com.aluminate.aluminate_organization_backend.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService implements IEventService{
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Event createEvent(CreateEventRequest request) {
        if (eventRepository.existsByTitleAndIsDeletedFalse(request.getTitle())) {
            throw new IllegalArgumentException("Event with this title '" + request.getTitle() +"' already exists!");
        }

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .status(EventStatus.DRAFT)
                .location(request.getLocation())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .maxParticipants(request.getMaxParticipants())
                .currentParticipants(0)
                .registrationDeadline(request.getRegistrationDeadline())
                .price(request.getPrice())
                .isPublic(request.isPublic())
                .requiresApproval(request.isRequiresApproval())
                .isDeleted(false)
                .build();

        return eventRepository.save(event);
    }

    @Override
    public List<EventResponseDTO> getAllEvents() {
        return eventRepository.findAll().stream()
                .filter(event -> !event.isDeleted())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private EventResponseDTO convertToDTO(Event event) {
        return EventResponseDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .type(event.getType())
                .status(event.getStatus())
                .location(event.getLocation())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .maxParticipants(event.getMaxParticipants())
                .currentParticipants(event.getCurrentParticipants())
                .registrationDeadline(event.getRegistrationDeadline())
                .price(event.getPrice())
                .isDeleted(event.isDeleted())
                .deletedAt(event.getDeletedAt())
                .build();
    }

    @Override
    public EventResponseDTO publishEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        if (EventStatus.PUBLISHED.equals(event.getStatus())) {
            throw new IllegalStateException("Event is already published");
        }

        if (EventStatus.CANCELLED.equals(event.getStatus())) {
            throw new IllegalStateException("Cannot publish a cancelled event");
        }

        if (EventStatus.COMPLETED.equals(event.getStatus())) {
            throw new IllegalStateException("Cannot publish a completed event");
        }

        event.setStatus(EventStatus.PUBLISHED);
        Event savedEvent = eventRepository.save(event);
        return convertToDTO(savedEvent);
    }


    @Override
    public EventResponseDTO cancelEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        if (EventStatus.CANCELLED.equals(event.getStatus())) {
            throw new IllegalStateException("Event is already cancelled");
        }

        if (EventStatus.COMPLETED.equals(event.getStatus())) {
            throw new IllegalStateException("Cannot cancel a completed event");
        }

        event.setStatus(EventStatus.CANCELLED);
        Event savedEvent = eventRepository.save(event);
        return convertToDTO(savedEvent);
    }

    @Override
    public EventResponseDTO deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        if (event.isDeleted()) {
            throw new IllegalStateException("Event is already deleted");
        }

        // Perform soft delete
        event.setDeleted(true);
        event.setDeletedAt(LocalDateTime.now());

//        // If the event has any members registered (MemberEvent relationships), you might want to handle them
//        if (!event.getMemberEvents().isEmpty()) {
//            // You might want to notify members that the event is deleted
//            // This is where you'd add that logic
//        }

        Event savedEvent = eventRepository.save(event);
        return convertToDTO(savedEvent);
    }

}
