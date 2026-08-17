package com.dropzone.searchservice.controller;

import com.dropzone.searchservice.model.EventDocument;
import com.dropzone.searchservice.service.OpenSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final OpenSearchService openSearchService;

    @GetMapping
    public ResponseEntity<List<EventDocument>> searchEvents(@RequestParam(value = "query", required = false, defaultValue = "coldplay") String query) {
        return ResponseEntity.ok(openSearchService.searchEvents(query));
    }

    @GetMapping("/formatted")
    public ResponseEntity<String> getFormattedSearchResults(@RequestParam(value = "query", required = false, defaultValue = "coldplay") String query) {
        return ResponseEntity.ok(openSearchService.getFormattedSearchResults(query));
    }

    @PostMapping("/index")
    public ResponseEntity<String> indexEventManually(@RequestBody EventDocument doc) {
        openSearchService.indexEvent(doc);
        return ResponseEntity.ok("Event indexed successfully in OpenSearch");
    }
}
