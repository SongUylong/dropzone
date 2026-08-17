package com.dropzone.eventservice.dto;

import com.dropzone.eventservice.model.EventStatus;
import jakarta.validation.Valid;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventRequest {

    private String title;
    private String description;
    private String venue;
    private OffsetDateTime eventDate;
    private OffsetDateTime saleStartTime;
    private OffsetDateTime saleEndTime;
    private EventStatus status;

    @Valid
    private List<CreateTicketCategoryRequest> ticketCategories;

    private List<String> imageUrls;
}
