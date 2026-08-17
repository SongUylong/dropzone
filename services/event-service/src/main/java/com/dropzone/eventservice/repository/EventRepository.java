package com.dropzone.eventservice.repository;

import com.dropzone.eventservice.model.Event;
import com.dropzone.eventservice.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatus(EventStatus status);
    List<Event> findByOrganizerId(String organizerId);
}
