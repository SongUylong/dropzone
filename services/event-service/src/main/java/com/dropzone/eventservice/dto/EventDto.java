package com.dropzone.eventservice.dto;

import com.dropzone.eventservice.model.EventStatus;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDto {
    private Long id;
    private String title;
    private String description;
    private String venue;
    private OffsetDateTime eventDate;
    private OffsetDateTime saleStartTime;
    private OffsetDateTime saleEndTime;
    private EventStatus status;
    private String organizerId;
    private List<TicketCategoryDto> ticketCategories;
    private List<EventImageDto> images;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
