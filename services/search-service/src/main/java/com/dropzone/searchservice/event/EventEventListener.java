package com.dropzone.searchservice.event;

import com.dropzone.searchservice.model.EventDocument;
import com.dropzone.searchservice.service.OpenSearchService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventEventListener {

    private final OpenSearchService openSearchService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "event-events", groupId = "search-service-group")
    public void handleEventEvent(String message) {
        log.info("[Search Service] Received Kafka event on topic 'event-events': {}", message);
        try {
            JsonNode node = objectMapper.readTree(message);
            Long id = node.has("id") ? node.get("id").asLong() : 1L;
            String title = node.has("title") ? node.get("title").asText() : (node.has("name") ? node.get("name").asText() : "Coldplay World Tour");
            String venue = node.has("venue") ? node.get("venue").asText() : (node.has("location") ? node.get("location").asText() : "National Stadium");
            String eventDate = node.has("eventDate") ? node.get("eventDate").asText() : (node.has("date") ? node.get("date").asText() : "October 10");
            String description = node.has("description") ? node.get("description").asText() : "";
            String status = node.has("status") ? node.get("status").asText() : "PUBLISHED";

            EventDocument doc = EventDocument.builder()
                    .id(id)
                    .title(title)
                    .name(title)
                    .venue(venue)
                    .location(venue)
                    .eventDate(eventDate)
                    .date(eventDate)
                    .description(description)
                    .status(status)
                    .build();

            openSearchService.indexEvent(doc);
            log.info("[Search Service] Processed EventUpdated/EventCreated event for ID {}: {}", id, title);
        } catch (Exception e) {
            log.error("[Search Service] Failed to process Kafka event event: {}", e.getMessage(), e);
        }
    }
}
