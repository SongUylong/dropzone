package com.dropzone.eventservice.service;

import com.dropzone.eventservice.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EventService {
    EventDto createEvent(CreateEventRequest request);
    EventDto updateEvent(Long id, UpdateEventRequest request);
    EventDto getEventById(Long id);
    List<EventDto> getAllEvents();
    void deleteEvent(Long id);

    EventImageDto uploadEventImage(Long eventId, MultipartFile file);
    FileUploadResponse uploadFileToMinio(String category, MultipartFile file);
}
