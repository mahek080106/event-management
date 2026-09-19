package com.gdg.event.repository;

import com.gdg.event.model.Event;
import com.gdg.event.model.EventStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EventRepository extends MongoRepository<Event, String> {

    Optional<Event> findByNameIgnoreCaseAndVenueIgnoreCaseAndStartDateTime(
            String name,
            String venue,
            LocalDateTime startDateTime
    );

    boolean existsByNameIgnoreCaseAndVenueIgnoreCaseAndStartDateTimeAndIdNot(
            String name,
            String venue,
            LocalDateTime startDateTime,
            String id
    );

    Page<Event> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    Page<Event> findByStatus(
            EventStatus status,
            Pageable pageable
    );

    Page<Event> findByNameContainingIgnoreCaseAndStatus(
            String name,
            EventStatus status,
            Pageable pageable
    );
}
