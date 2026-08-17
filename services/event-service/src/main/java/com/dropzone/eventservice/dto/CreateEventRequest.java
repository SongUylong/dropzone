package com.dropzone.eventservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEventRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Venue is required")
    private String venue;

    @NotNull(message = "Event date is required")
    private OffsetDateTime eventDate;

    private OffsetDateTime saleStartTime;

    private OffsetDateTime saleEndTime;

    private String organizerId;

    @NotEmpty(message = "At least one ticket category is required")
    @Valid
    private List<CreateTicketCategoryRequest> ticketCategories;

    private List<String> imageUrls;
}
