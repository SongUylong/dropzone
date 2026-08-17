package com.dropzone.eventservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEvent {
    private String eventType;
    private Long id;
    private String title;
    private String name;
    private String description;
    private String venue;
    private String location;
    private String date;
    private String eventDate;
    private String status;
    private Instant timestamp;
}
