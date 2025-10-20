// src/main/java/com/aluminate/aluminate_organization_backend/service/event/EventService.java
package com.aluminate.aluminate_organization_backend.service.event;

import com.aluminate.aluminate_organization_backend.dto.event.*;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.model.Event;
import com.aluminate.aluminate_organization_backend.model.EventStatus;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.MemberEvent;
import com.aluminate.aluminate_organization_backend.model.NotificationRequest;
import com.aluminate.aluminate_organization_backend.repository.EventRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberEventRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import com.aluminate.aluminate_organization_backend.service.notification.NotificationProducer;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService implements IEventService {

    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final MemberEventRepository memberEventRepository;

    // Notify members on publish
    private final NotificationProducer notificationProducer;

    // Defaults are safe; override via application.properties if needed
    @Value("${aluminate.tenant-id:org-demo}")
    private String tenantId;

    // Used to build the CTA link inside the notification
    @Value("${aluminate.portal-base-url:http://localhost:3000}")
    private String portalBaseUrl;

    public EventService(EventRepository eventRepository,
                        MemberRepository memberRepository,
                        MemberEventRepository memberEventRepository,
                        NotificationProducer notificationProducer) {
        this.eventRepository = eventRepository;
        this.memberRepository = memberRepository;
        this.memberEventRepository = memberEventRepository;
        this.notificationProducer = notificationProducer;
    }

    @Override
    public Event createEvent(CreateEventRequest request) {
        if (eventRepository.existsByTitleAndIsDeletedFalse(request.getTitle())) {
            throw new IllegalArgumentException("Event with this title '" + request.getTitle() + "' already exists!");
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
    @Transactional
    public EventResponseDTO updateEvent(Long id, UpdateEventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        if (event.isDeleted()) {
            throw new IllegalStateException("Cannot edit a deleted event");
        }

        if (request.getTitle() != null
                && !request.getTitle().equals(event.getTitle())
                && eventRepository.existsByTitleAndIsDeletedFalseAndIdNot(request.getTitle(), id)) {
            throw new IllegalArgumentException("Another active event with this title already exists");
        }

        if (request.getMaxParticipants() != null
                && request.getMaxParticipants() < event.getCurrentParticipants()) {
            throw new IllegalStateException("maxParticipants cannot be less than currentParticipants");
        }

        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getType() != null) event.setType(request.getType());
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getStartDate() != null) event.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) event.setEndDate(request.getEndDate());
        if (request.getStartTime() != null) event.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) event.setEndTime(request.getEndTime());
        if (request.getMaxParticipants() != null) event.setMaxParticipants(request.getMaxParticipants());
        if (request.getRegistrationDeadline() != null) event.setRegistrationDeadline(request.getRegistrationDeadline());
        if (request.getPrice() != null) event.setPrice(request.getPrice());
        if (request.getIsPublic() != null) event.setPublic(request.getIsPublic());
        if (request.getRequiresApproval() != null) event.setRequiresApproval(request.getRequiresApproval());

        Event saved = eventRepository.save(event);
        return convertToDTO(saved);
    }

    @Override
    public List<EventResponseDTO> getAllEvents() {
        return eventRepository.findAll().stream()
                .filter(event -> !event.isDeleted())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public long getEventCount() {
        return eventRepository.count();
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

        // === Broadcast in-app notifications to members ===
        // (Scope this query to your tenant/active members if needed)
        List<Member> recipients = memberRepository.findAll();

        String start = (savedEvent.getStartDate() != null ? savedEvent.getStartDate().toString() : "")
                + (savedEvent.getStartTime() != null ? (" " + savedEvent.getStartTime().toString()) : "");

        // Link users to your member events page (or deep-link if you have one)
        String ctaUrl = portalBaseUrl + "/org/member/events";

        for (Member m : recipients) {
            NotificationRequest req = NotificationRequest.builder()
                    .messageId(UUID.randomUUID().toString())
                    .tenantId(tenantId)
                    .memberId(String.valueOf(m.getId()))
                    // Use Set to match typical model; avoids List→Set cast issues
                    .channels(Set.of("IN_APP"))
                    .type("EVENT_PUBLISHED")
                    .template("event_published")
                    .data(Map.of(
                            "eventId", String.valueOf(savedEvent.getId()),
                            "eventName", savedEvent.getTitle(),
                            "start", start,
                            "ctaUrl", ctaUrl
                    ))
                    .dedupeKey("EVENT_PUBLISHED:" + savedEvent.getId() + ":" + m.getId())
                    .createdAt(Instant.now())
                    .build();

            notificationProducer.send(req); // -> Kafka topic via NotificationProducer
        }

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

        // OPTIONAL: notify members about cancellation
        // List<Member> recipients = memberRepository.findAll();
        // for (Member m : recipients) {
        //     NotificationRequest req = NotificationRequest.builder()
        //             .messageId(UUID.randomUUID().toString())
        //             .tenantId(tenantId)
        //             .memberId(String.valueOf(m.getId()))
        //             .channels(Set.of("IN_APP"))
        //             .type("EVENT_CANCELLED")
        //             .template("event_cancelled")
        //             .data(Map.of(
        //                     "eventId", String.valueOf(savedEvent.getId()),
        //                     "eventName", savedEvent.getTitle(),
        //                     "ctaUrl", portalBaseUrl + "/org/member/events"
        //             ))
        //             .dedupeKey("EVENT_CANCELLED:" + savedEvent.getId() + ":" + m.getId())
        //             .createdAt(Instant.now())
        //             .build();
        //     notificationProducer.send(req);
        // }

        return convertToDTO(savedEvent);
    }

    @Override
    public EventResponseDTO deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        if (event.isDeleted()) {
            throw new IllegalStateException("Event is already deleted");
        }

        event.setDeleted(true);
        event.setDeletedAt(LocalDateTime.now());

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
            link.setAttending(true);
            link.setSetRSVP(true);
            memberEventRepository.save(link);
        } else {
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

        link.setAttending(false);
        link.setSetRSVP(false);
        memberEventRepository.save(link);

        if (event.getCurrentParticipants() > 0) {
            event.setCurrentParticipants(event.getCurrentParticipants() - 1);
        }
        Event savedEvent = eventRepository.save(event);

        return convertToDTO(savedEvent);
    }

    @Override
    public List<MemberAttendanceDTO> getEventAttendance(Long eventId) {
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
        memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        List<Event> events = eventRepository.findAll().stream()
                .filter(e -> !e.isDeleted())
                .toList();

        Set<Long> attendedEventIds = memberEventRepository.findAllByMemberIdAndAttendingTrue(memberId).stream()
                .map(MemberEvent::getEvent)
                .map(Event::getId)
                .collect(Collectors.toSet());

        return events.stream()
                .map(e -> new EventAttendanceStatusDTO(e.getId(), attendedEventIds.contains(e.getId())))
                .toList();
    }

    @Override
    public byte[] exportEventAttendeesCsv(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        List<MemberEvent> links = memberEventRepository.findAllByEvent_IdAndIsAttendingTrue(eventId);

        StringBuilder sb = new StringBuilder();
        sb.append("EventId,EventTitle,MemberId,MemberName,MemberEmail,RSVP,Attending\n");

        for (MemberEvent me : links) {
            Member m = me.getMember();
            String name = m != null ? safeCsv(m.getName()) : "";
            String email = m != null ? safeCsv(m.getEmail()) : "";

            sb.append(event.getId()).append(',')
                    .append(safeCsv(event.getTitle())).append(',')
                    .append(m != null ? m.getId() : "").append(',')
                    .append(name).append(',')
                    .append(email).append(',')
                    .append(me.isSetRSVP()).append(',')
                    .append(me.isAttending()).append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String safeCsv(String value) {
        if (value == null) return "";
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }
}
