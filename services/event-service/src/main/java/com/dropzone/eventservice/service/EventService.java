package com.dropzone.eventservice.service;

import com.dropzone.eventservice.dto.CreateEventRequest;
import com.dropzone.eventservice.dto.EventDto;
import com.dropzone.eventservice.dto.UpdateEventRequest;

import java.util.List;

public interface EventService {
    EventDto createEvent(CreateEventRequest request);
    EventDto updateEvent(Long id, UpdateEventRequest request);
    EventDto getEventById(Long id);
    List<EventDto> getAllEvents();
    void deleteEvent(Long id);
}
