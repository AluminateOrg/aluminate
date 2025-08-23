package com.aluminate.aluminate_organization_backend.controller;


import com.aluminate.aluminate_organization_backend.dto.event.*;
import com.aluminate.aluminate_organization_backend.dto.response.ApiResponse;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.model.Event;
import com.aluminate.aluminate_organization_backend.service.event.IEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}")
public class EventController {
    private final IEventService eventService;

    // ADMIN ONLY
    @PostMapping("/admin/event/create")
    public ResponseEntity<ApiResponse> createEvent(@RequestBody CreateEventRequest request) {
        try {
            Event createdEvent = eventService.createEvent(request);
            return ResponseEntity.ok(new ApiResponse("Event created successfully", createdEvent));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(CONFLICT)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to create event", null));
        }
    }

    // ADMIN ONLY: edit event (partial update)
    @PutMapping("/admin/event/{id}/edit")
    public ResponseEntity<ApiResponse> updateEvent(@PathVariable Long id, @RequestBody UpdateEventRequest request) {
        try {
            EventResponseDTO updated = eventService.updateEvent(id, request);
            return ResponseEntity.ok(new ApiResponse("Event updated successfully", updated));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Failed to update event", null));
        }
    }

    // BOTH MEMBER AND ADMIN
    @GetMapping({"/admin/event/get/all", "/member/event/get/all", "/common/event/get/all"})
    public ResponseEntity<ApiResponse> getAllEvents() {
        try {
            List<EventResponseDTO> events = eventService.getAllEvents();
            return ResponseEntity.ok(new ApiResponse("Events retrieved successfully!", events));
        } catch (Exception e) {
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve all events", null));
        }
    }

    // ADMIN ONLY
    @PutMapping("/admin/event/{id}/publish")
    public ResponseEntity<ApiResponse> publishEvent(@PathVariable Long id) {
        try {
            EventResponseDTO publishedEvent = eventService.publishEvent(id);
            return ResponseEntity.ok(new ApiResponse("Event published successfully", publishedEvent));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(CONFLICT)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to publish event", null));
        }
    }

    // ADMIN ONLY
    @PutMapping("/admin/event/{id}/cancel")
    public ResponseEntity<ApiResponse> cancelEvent(@PathVariable Long id) {
        try {
            EventResponseDTO cancelledEvent = eventService.cancelEvent(id);
            return ResponseEntity.ok(new ApiResponse("Event cancelled successfully", cancelledEvent));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(CONFLICT)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to cancel event", null));
        }
    }

    // ADMIN ONLY
    @DeleteMapping("/admin/event/{id}/delete")
    public ResponseEntity<ApiResponse> deleteEvent(@PathVariable Long id) {
        try {
            EventResponseDTO deletedEvent = eventService.deleteEvent(id);
            return ResponseEntity.ok(new ApiResponse("Event deleted successfully", deletedEvent));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(CONFLICT)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to delete event", null));
        }
    }

    // MEMBER ONLY
    @PostMapping("/member/event/{eventId}/attend/{memberId}")
    public ResponseEntity<ApiResponse> attendEvent(@PathVariable Long eventId, @PathVariable Long memberId) {
        try {
            EventResponseDTO updated = eventService.attendEvent(eventId, memberId);
            return ResponseEntity.ok(new ApiResponse("Attendance recorded successfully", updated));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Failed to attend event", null));
        }
    }

    // MEMBER ONLY: mark not attending
    @PutMapping("/member/event/{eventId}/unattend/{memberId}")
    public ResponseEntity<ApiResponse> unattendEvent(@PathVariable Long eventId, @PathVariable Long memberId) {
        try {
            EventResponseDTO updated = eventService.unattendEvent(eventId, memberId);
            return ResponseEntity.ok(new ApiResponse("Attendance removed successfully", updated));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Failed to remove attendance", null));
        }
    }


    // BOTH ADMIN AND MEMBER: list attendees for an event
    @GetMapping({"/admin/event/{eventId}/attendances", "/member/event/{eventId}/attendances"})
    public ResponseEntity<ApiResponse> getEventAttendance(@PathVariable Long eventId) {
        try {
            List<MemberAttendanceDTO> attendees = eventService.getEventAttendance(eventId);
            return ResponseEntity.ok(new ApiResponse("Attendance list retrieved successfully", attendees));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Failed to retrieve attendance list", null));
        }
    }

    // BOTH ADMIN AND MEMBER: list events a member attends
    @GetMapping({"/common/event/attendances/{memberId}", "/member/event/attendances/{memberId}"})
    public ResponseEntity<ApiResponse> memberAttendances(@PathVariable Long memberId) {
        try {
            List<EventResponseDTO> events = eventService.getMemberAttendances(memberId);
            return ResponseEntity.ok(new ApiResponse("Member attendances retrieved successfully", events));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Failed to retrieve member attendances", null));
        }
    }

    // BOTH ADMIN AND MEMBER: for a member, list all events with attending flag
    @GetMapping({"/common/event/attendance-status/{memberId}", "/member/event/attendance-status/{memberId}"})
    public ResponseEntity<ApiResponse> getMemberEventAttendanceStatuses(@PathVariable Long memberId) {
        try {
            List<EventAttendanceStatusDTO> statuses = eventService.getMemberEventAttendanceStatuses(memberId);
            return ResponseEntity.ok(new ApiResponse("Attendance statuses retrieved successfully", statuses));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Failed to retrieve attendance statuses", null));
        }
    }

    // ADMIN ONLY: export attendees of an event as CSV
    @GetMapping("/admin/event/{eventId}/attendees/export")
    public ResponseEntity<byte[]> exportEventAttendees(@PathVariable Long eventId) {
        try {
            byte[] csv = eventService.exportEventAttendeesCsv(eventId);

            String filename = "event_" + eventId + "_attendees.csv";
            String contentDisposition = "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" +
                    URLEncoder.encode(filename, StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                    .body(csv);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("Event not found").getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("Failed to export attendees").getBytes(StandardCharsets.UTF_8));
        }
    }



}
