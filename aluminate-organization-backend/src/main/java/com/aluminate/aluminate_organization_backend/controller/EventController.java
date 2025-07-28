package com.aluminate.aluminate_organization_backend.controller;


import com.aluminate.aluminate_organization_backend.dto.event.CreateEventRequest;
import com.aluminate.aluminate_organization_backend.dto.event.EventResponseDTO;
import com.aluminate.aluminate_organization_backend.dto.response.ApiResponse;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.model.Event;
import com.aluminate.aluminate_organization_backend.service.event.IEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/event")
public class EventController {
    private final IEventService eventService;

    @PostMapping("/create")
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

    @GetMapping("/get/all")
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

    @PutMapping("/{id}/publish")
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

    @PutMapping("/{id}/cancel")
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

    @DeleteMapping("/{id}/delete")
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
}
