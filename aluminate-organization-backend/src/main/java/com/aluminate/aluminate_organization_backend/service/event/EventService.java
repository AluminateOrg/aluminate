package com.aluminate.aluminate_organization_backend.service.event;

import com.aluminate.aluminate_organization_backend.dto.event.CreateEventRequest;
import com.aluminate.aluminate_organization_backend.dto.event.EventAttendanceStatusDTO;
import com.aluminate.aluminate_organization_backend.dto.event.EventResponseDTO;
import com.aluminate.aluminate_organization_backend.dto.event.MemberAttendanceDTO;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.model.Event;
import com.aluminate.aluminate_organization_backend.model.EventStatus;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.MemberEvent;
import com.aluminate.aluminate_organization_backend.repository.EventRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberEventRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventService implements IEventService{
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final MemberEventRepository memberEventRepository;

    public EventService(EventRepository eventRepository, MemberRepository memberRepository, MemberEventRepository memberEventRepository) {
        this.eventRepository = eventRepository;
        this.memberRepository = memberRepository;
        this.memberEventRepository = memberEventRepository;

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

    @Override
    @Transactional
    public EventResponseDTO attendEvent(Long eventId, Long memberId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (event.isDeleted()) {
            throw new IllegalStateException("Cannot attend a deleted event");
        }
        if (!EventStatus.PUBLISHED.equals(event.getStatus())) {
            throw new IllegalStateException("Only published events can be attended");
        }
        if (event.getRegistrationDeadline() != null && LocalDate.now().isAfter(event.getRegistrationDeadline())) {
            throw new IllegalStateException("Registration deadline has passed");
        }
        if (event.getCurrentParticipants() >= event.getMaxParticipants()) {
            throw new IllegalStateException("Event is full");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        MemberEvent link = memberEventRepository.findByMemberIdAndEventId(member.getId(), event.getId())
                .orElse(null);

        if (link != null) {
            if (link.isAttending()) {
                throw new IllegalStateException("Member is already attending this event");
            }
            // Not attending yet -> mark attending and increment count
            link.setAttending(true);
            link.setSetRSVP(true);
            memberEventRepository.save(link);
        } else {
            // New attendance link
            MemberEvent newLink = new MemberEvent();
            newLink.setMember(member);
            newLink.setEvent(event);
            newLink.setAttending(true);
            newLink.setSetRSVP(true);
            memberEventRepository.save(newLink);
        }

        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        Event savedEvent = eventRepository.save(event);

        return convertToDTO(savedEvent);
    }

    @Override
    @Transactional
    public EventResponseDTO unattendEvent(Long eventId, Long memberId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (event.isDeleted()) {
            throw new IllegalStateException("Cannot change attendance on a deleted event");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        MemberEvent link = memberEventRepository.findByMemberIdAndEventId(member.getId(), event.getId())
                .orElseThrow(() -> new IllegalStateException("Member is not registered for this event"));

        if (!link.isAttending()) {
            throw new IllegalStateException("Member is not attending this event");
        }

        // Flip attendance off
        link.setAttending(false);
        link.setSetRSVP(false);
        memberEventRepository.save(link);

        // Decrement count safely
        if (event.getCurrentParticipants() > 0) {
            event.setCurrentParticipants(event.getCurrentParticipants() - 1);
        }
        Event savedEvent = eventRepository.save(event);

        return convertToDTO(savedEvent);
    }


    @Override
    public List<MemberAttendanceDTO> getEventAttendance(Long eventId) {
        // validate event exists
        eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        return memberEventRepository.findAllByEventId(eventId).stream()
                .map(me -> new MemberAttendanceDTO(
                        me.getMember().getId(),
                        me.isAttending(),
                        me.isSetRSVP()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventResponseDTO> getMemberAttendances(Long memberId) {
        // validate member exists
        memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        return memberEventRepository.findAllByMemberIdAndAttendingTrue(memberId).stream()
                .map(MemberEvent::getEvent)
                .filter(e -> !e.isDeleted())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventAttendanceStatusDTO> getMemberEventAttendanceStatuses(Long memberId) {
        // Ensure member exists
        memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        // Load all non-deleted events
        List<Event> events = eventRepository.findAll().stream()
                .filter(e -> !e.isDeleted())
                .toList();

        // Load all attending links for the member
        Set<Long> attendedEventIds = memberEventRepository.findAllByMemberIdAndAttendingTrue(memberId).stream()
                .map(MemberEvent::getEvent)
                .map(Event::getId)
                .collect(Collectors.toSet());

        // Build statuses for each event
        return events.stream()
                .map(e -> new EventAttendanceStatusDTO(e.getId(), attendedEventIds.contains(e.getId())))
                .toList();
    }



}
