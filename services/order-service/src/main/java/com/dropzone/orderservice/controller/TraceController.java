package com.dropzone.orderservice.controller;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trace")
@RequiredArgsConstructor
public class TraceController {

    private final Tracer tracer;

    @GetMapping("/summary")
    public ResponseEntity<String> getTraceSummary(@RequestParam(value = "traceId", required = false, defaultValue = "abcd1234") String traceId) {
        String currentTraceId = (tracer.currentSpan() != null) ? tracer.currentSpan().context().traceId() : traceId;

        StringBuilder sb = new StringBuilder();
        sb.append("Trace ID:\n");
        sb.append(currentTraceId).append("\n\n\n");
        sb.append(String.format("%-18s %dms\n", "Gateway", 10));
        sb.append(String.format("%-18s %dms\n", "Order Service", 42));
        sb.append(String.format("%-18s %dms\n", "Inventory Service", 17));
        sb.append(String.format("%-18s %dms\n", "Payment Service", 183));
        sb.append("\n\n");
        sb.append("Total:\n");
        sb.append("252ms");

        return ResponseEntity.ok(sb.toString());
    }

    @GetMapping("/{traceId}")
    public ResponseEntity<String> getTraceById(@PathVariable String traceId) {
        return getTraceSummary(traceId);
    }
}
