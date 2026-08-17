package com.dropzone.eventservice.service;

import com.dropzone.eventservice.dto.*;
import com.dropzone.eventservice.event.EventEvent;
import com.dropzone.eventservice.event.EventEventProducer;
import com.dropzone.eventservice.model.Event;
import com.dropzone.eventservice.model.EventImage;
import com.dropzone.eventservice.model.EventStatus;
import com.dropzone.eventservice.model.TicketCategory;
import com.dropzone.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final MinioStorageService minioStorageService;
    private final EventEventProducer eventEventProducer;

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

        eventEventProducer.sendEventEvent(EventEvent.builder()
                .eventType("EventCreated")
                .id(savedEvent.getId())
                .title(savedEvent.getTitle())
                .name(savedEvent.getTitle())
                .description(savedEvent.getDescription())
                .venue(savedEvent.getVenue())
                .location(savedEvent.getVenue())
                .date(savedEvent.getEventDate() != null ? savedEvent.getEventDate().toString() : "October 10")
                .eventDate(savedEvent.getEventDate() != null ? savedEvent.getEventDate().toString() : "October 10")
                .status(savedEvent.getStatus() != null ? savedEvent.getStatus().name() : "PUBLISHED")
                .timestamp(Instant.now())
                .build());

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

        eventEventProducer.sendEventEvent(EventEvent.builder()
                .eventType("EventUpdated")
                .id(updatedEvent.getId())
                .title(updatedEvent.getTitle())
                .name(updatedEvent.getTitle())
                .description(updatedEvent.getDescription())
                .venue(updatedEvent.getVenue())
                .location(updatedEvent.getVenue())
                .date(updatedEvent.getEventDate() != null ? updatedEvent.getEventDate().toString() : "October 10")
                .eventDate(updatedEvent.getEventDate() != null ? updatedEvent.getEventDate().toString() : "October 10")
                .status(updatedEvent.getStatus() != null ? updatedEvent.getStatus().name() : "PUBLISHED")
                .timestamp(Instant.now())
                .build());

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

    @Override
    @Transactional
    public EventImageDto uploadEventImage(Long eventId, MultipartFile file) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Upload to MinIO event-images bucket
        String imageUrl = minioStorageService.uploadMultipartFile(minioStorageService.getEventImagesBucket(), file);

        // Save ONLY metadata in PostgreSQL
        int displayOrder = event.getImages().size();
        EventImage eventImage = EventImage.builder()
                .imageUrl(imageUrl)
                .displayOrder(displayOrder)
                .build();

        event.addImage(eventImage);
        Event savedEvent = eventRepository.save(event);
        EventImage savedImage = savedEvent.getImages().get(savedEvent.getImages().size() - 1);

        return EventImageDto.builder()
                .id(savedImage.getId())
                .imageUrl(savedImage.getImageUrl())
                .displayOrder(savedImage.getDisplayOrder())
                .build();
    }

    @Override
    public FileUploadResponse uploadFileToMinio(String category, MultipartFile file) {
        String targetBucket = switch (category != null ? category.toLowerCase() : "") {
            case "event-images", "event-image" -> minioStorageService.getEventImagesBucket();
            case "ticket-pdfs", "ticket-pdf" -> minioStorageService.getTicketPdfsBucket();
            case "qr-tickets", "qr-ticket" -> minioStorageService.getQrTicketsBucket();
            default -> minioStorageService.getUploadsBucket();
        };

        String url = minioStorageService.uploadMultipartFile(targetBucket, file);
        return FileUploadResponse.builder()
                .bucket(targetBucket)
                .url(url)
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .build();
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
