package com.dropzone.eventservice.service;

import com.dropzone.eventservice.dto.*;
import com.dropzone.eventservice.model.Event;
import com.dropzone.eventservice.model.EventImage;
import com.dropzone.eventservice.model.EventStatus;
import com.dropzone.eventservice.model.TicketCategory;
import com.dropzone.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    @Transactional
    public EventDto createEvent(CreateEventRequest request) {
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .venue(request.getVenue())
                .eventDate(request.getEventDate())
                .saleStartTime(request.getSaleStartTime())
                .saleEndTime(request.getSaleEndTime())
                .status(EventStatus.DRAFT)
                .organizerId(request.getOrganizerId())
                .build();

        if (request.getTicketCategories() != null) {
            for (CreateTicketCategoryRequest catReq : request.getTicketCategories()) {
                TicketCategory category = TicketCategory.builder()
                        .name(catReq.getName())
                        .price(catReq.getPrice())
                        .totalQuantity(catReq.getTotalQuantity())
                        .availableQuantity(catReq.getTotalQuantity())
                        .build();
                event.addTicketCategory(category);
            }
        }

        if (request.getImageUrls() != null) {
            int order = 0;
            for (String url : request.getImageUrls()) {
                EventImage image = EventImage.builder()
                        .imageUrl(url)
                        .displayOrder(order++)
                        .build();
                event.addImage(image);
            }
        }

        Event savedEvent = eventRepository.save(event);
        return mapToDto(savedEvent);
    }

    @Override
    @Transactional
    public EventDto updateEvent(Long id, UpdateEventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getVenue() != null) {
            event.setVenue(request.getVenue());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getSaleStartTime() != null) {
            event.setSaleStartTime(request.getSaleStartTime());
        }
        if (request.getSaleEndTime() != null) {
            event.setSaleEndTime(request.getSaleEndTime());
        }
        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        }

        if (request.getTicketCategories() != null) {
            event.getTicketCategories().clear();
            for (CreateTicketCategoryRequest catReq : request.getTicketCategories()) {
                TicketCategory category = TicketCategory.builder()
                        .name(catReq.getName())
                        .price(catReq.getPrice())
                        .totalQuantity(catReq.getTotalQuantity())
                        .availableQuantity(catReq.getTotalQuantity())
                        .build();
                event.addTicketCategory(category);
            }
        }

        if (request.getImageUrls() != null) {
            event.getImages().clear();
            int order = 0;
            for (String url : request.getImageUrls()) {
                EventImage image = EventImage.builder()
                        .imageUrl(url)
                        .displayOrder(order++)
                        .build();
                event.addImage(image);
            }
        }

        Event updatedEvent = eventRepository.save(event);
        return mapToDto(updatedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public EventDto getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        return mapToDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDto> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }

    private EventDto mapToDto(Event event) {
        List<TicketCategoryDto> categoryDtos = event.getTicketCategories() != null ?
                event.getTicketCategories().stream()
                        .map(c -> TicketCategoryDto.builder()
                                .id(c.getId())
                                .name(c.getName())
                                .price(c.getPrice())
                                .totalQuantity(c.getTotalQuantity())
                                .availableQuantity(c.getAvailableQuantity())
                                .build())
                        .collect(Collectors.toList()) : List.of();

        List<EventImageDto> imageDtos = event.getImages() != null ?
                event.getImages().stream()
                        .map(img -> EventImageDto.builder()
                                .id(img.getId())
                                .imageUrl(img.getImageUrl())
                                .displayOrder(img.getDisplayOrder())
                                .build())
                        .collect(Collectors.toList()) : List.of();

        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .venue(event.getVenue())
                .eventDate(event.getEventDate())
                .saleStartTime(event.getSaleStartTime())
                .saleEndTime(event.getSaleEndTime())
                .status(event.getStatus())
                .organizerId(event.getOrganizerId())
                .ticketCategories(categoryDtos)
                .images(imageDtos)
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
