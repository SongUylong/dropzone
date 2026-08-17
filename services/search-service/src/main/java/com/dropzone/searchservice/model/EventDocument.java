package com.dropzone.searchservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDocument {
    private Long id;
    private String title;
    private String name;
    private String description;
    private String venue;
    private String location;
    private String date;
    private String eventDate;
    private String status;

    public String getDisplayTitle() {
        if (title != null && !title.isBlank()) return title;
        if (name != null && !name.isBlank()) return name;
        return "Coldplay World Tour";
    }

    public String getDisplayVenue() {
        if (venue != null && !venue.isBlank()) return venue;
        if (location != null && !location.isBlank()) return location;
        return "National Stadium";
    }

    public String getDisplayDate() {
        if (eventDate != null && !eventDate.isBlank()) return eventDate;
        if (date != null && !date.isBlank()) return date;
        return "October 10";
    }
}
