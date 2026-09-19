package com.gdg.event.service;

import com.gdg.event.dto.CreateEventRequest;
import com.gdg.event.dto.EventResponse;
import com.gdg.event.dto.UpdateEventRequest;
import com.gdg.event.exception.BusinessRuleException;
import com.gdg.event.exception.DuplicateEventException;
import com.gdg.event.exception.EventNotFoundException;
import com.gdg.event.model.Event;
import com.gdg.event.model.EventStatus;
import com.gdg.event.repository.EventRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;


    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }


    public EventResponse createEvent(CreateEventRequest request) {

        if (!request.getEndDateTime().isAfter(request.getStartDateTime())) {
            throw new BusinessRuleException(
                    "End date and time must be after start date and time"
            );
        }


        boolean duplicate = eventRepository
                .findByNameIgnoreCaseAndVenueIgnoreCaseAndStartDateTime(
                        request.getName(),
                        request.getVenue(),
                        request.getStartDateTime()
                )
                .isPresent();

        if (duplicate) {
            throw new DuplicateEventException(
                    "An event with the same name, venue and start time already exists"
            );
        }


        Event event = new Event();

        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setStartDateTime(request.getStartDateTime());
        event.setEndDateTime(request.getEndDateTime());
        event.setVenue(request.getVenue());
        event.setCapacity(request.getCapacity());
        event.setStatus(EventStatus.UPCOMING);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());

        Event savedEvent = eventRepository.save(event);

        return convertToResponse(savedEvent);
    }


    public EventResponse getEventById(String id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException("Event not found with id: " + id)
                );

        updateStatus(event);

        return convertToResponse(event);
    }


    public Page<EventResponse> getAllEvents(
            String search,
            EventStatus status,
            Pageable pageable) {

        Page<Event> events;


        if (search != null && !search.isBlank() && status != null) {

            events = eventRepository.findByNameContainingIgnoreCaseAndStatus(
                    search,
                    status,
                    pageable
            );

        } else if (search != null && !search.isBlank()) {

            events = eventRepository.findByNameContainingIgnoreCase(
                    search,
                    pageable
            );

        } else if (status != null) {

            events = eventRepository.findByStatus(
                    status,
                    pageable
            );

        } else {

            events = eventRepository.findAll(pageable);
        }


        List<EventResponse> responseList = events
                .getContent()
                .stream()
                .map(event -> {
                    updateStatus(event);
                    return convertToResponse(event);
                })
                .toList();

        return new PageImpl<>(
                responseList,
                pageable,
                events.getTotalElements()
        );
    }


    public EventResponse updateEvent(
            String id,
            UpdateEventRequest request) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException("Event not found with id: " + id)
                );


        updateStatus(event);


        if (event.getStatus() == EventStatus.ONGOING) {
            throw new BusinessRuleException(
                    "Ongoing events cannot be edited"
            );
        }

        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Completed events cannot be edited"
            );
        }

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "Cancelled events cannot be edited"
            );
        }


        if (!request.getEndDateTime().isAfter(request.getStartDateTime())) {
            throw new BusinessRuleException(
                    "End date and time must be after start date and time"
            );
        }


        boolean duplicate = eventRepository
                .existsByNameIgnoreCaseAndVenueIgnoreCaseAndStartDateTimeAndIdNot(
                        request.getName(),
                        request.getVenue(),
                        request.getStartDateTime(),
                        id
                );

        if (duplicate) {
            throw new DuplicateEventException(
                    "Another event with the same name, venue and start time already exists"
            );
        }


        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setStartDateTime(request.getStartDateTime());
        event.setEndDateTime(request.getEndDateTime());
        event.setVenue(request.getVenue());
        event.setCapacity(request.getCapacity());
        event.setUpdatedAt(LocalDateTime.now());

        Event updatedEvent = eventRepository.save(event);

        return convertToResponse(updatedEvent);
    }


    public void deleteEvent(String id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException("Event not found with id: " + id)
                );


        updateStatus(event);


        if (event.getStatus() == EventStatus.ONGOING) {
            throw new BusinessRuleException(
                    "Ongoing events cannot be deleted"
            );
        }

        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Completed events cannot be deleted"
            );
        }


        eventRepository.delete(event);
    }


    public EventResponse cancelEvent(String id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException("Event not found with id: " + id)
                );


        updateStatus(event);


        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Completed events cannot be cancelled"
            );
        }

        if (event.getStatus() == EventStatus.ONGOING) {
            throw new BusinessRuleException(
                    "Ongoing events cannot be cancelled"
            );
        }


        event.setStatus(EventStatus.CANCELLED);
        event.setUpdatedAt(LocalDateTime.now());

        Event savedEvent = eventRepository.save(event);

        return convertToResponse(savedEvent);
    }


    private void updateStatus(Event event) {

        if (event.getStatus() == EventStatus.CANCELLED) {
            return;
        }


        LocalDateTime now = LocalDateTime.now();


        if (now.isBefore(event.getStartDateTime())) {

            event.setStatus(EventStatus.UPCOMING);

        } else if (now.isBefore(event.getEndDateTime())) {

            event.setStatus(EventStatus.ONGOING);

        } else {

            event.setStatus(EventStatus.COMPLETED);
        }
    }


    private EventResponse convertToResponse(Event event) {

        EventResponse response = new EventResponse();

        response.setId(event.getId());
        response.setName(event.getName());
        response.setDescription(event.getDescription());
        response.setStartDateTime(event.getStartDateTime());
        response.setEndDateTime(event.getEndDateTime());
        response.setVenue(event.getVenue());
        response.setCapacity(event.getCapacity());
        response.setStatus(event.getStatus());
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());

        return response;
    }
}