package com.gdg.event.controller;

import com.gdg.event.dto.CreateEventRequest;
import com.gdg.event.dto.EventResponse;
import com.gdg.event.dto.UpdateEventRequest;
import com.gdg.event.model.EventStatus;
import com.gdg.event.service.EventService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;


    public EventController(EventService eventService) {
        this.eventService = eventService;
    }


    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request) {

        EventResponse response = eventService.createEvent(request);

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }


    @GetMapping
    public ResponseEntity<Page<EventResponse>> getAllEvents(

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            EventStatus status,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "startDateTime")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String direction) {


        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Size must be between 1 and 100"
            );
        }


        if (!sortBy.equals("name")
                && !sortBy.equals("startDateTime")
                && !sortBy.equals("capacity")) {

            throw new IllegalArgumentException(
                    "Invalid sort field. Use name, startDateTime or capacity"
            );
        }


        Sort.Direction sortDirection;

        if (direction.equalsIgnoreCase("desc")) {
            sortDirection = Sort.Direction.DESC;
        } else if (direction.equalsIgnoreCase("asc")) {
            sortDirection = Sort.Direction.ASC;
        } else {
            throw new IllegalArgumentException(
                    "Direction must be asc or desc"
            );
        }


        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );


        Page<EventResponse> response =
                eventService.getAllEvents(
                        search,
                        status,
                        pageable
                );


        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable String id) {

        EventResponse response = eventService.getEventById(id);

        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable String id,
            @Valid @RequestBody UpdateEventRequest request) {

        EventResponse response =
                eventService.updateEvent(id, request);

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{id}/cancel")
    public ResponseEntity<EventResponse> cancelEvent(
            @PathVariable String id) {

        EventResponse response =
                eventService.cancelEvent(id);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable String id) {

        eventService.deleteEvent(id);

        return ResponseEntity.noContent().build();
    }
}