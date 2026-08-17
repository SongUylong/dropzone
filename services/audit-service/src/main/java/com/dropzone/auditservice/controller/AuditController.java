package com.dropzone.auditservice.controller;

import com.dropzone.auditservice.model.AuditRecord;
import com.dropzone.auditservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/logs")
    public ResponseEntity<List<AuditRecord>> getAllAuditRecords() {
        return ResponseEntity.ok(auditService.getAllAuditRecords());
    }

    @GetMapping("/logs/topic/{topic}")
    public ResponseEntity<List<AuditRecord>> getAuditRecordsByTopic(@PathVariable String topic) {
        return ResponseEntity.ok(auditService.getAuditRecordsByTopic(topic));
    }

    @GetMapping("/logs/order/{orderNumber}")
    public ResponseEntity<List<AuditRecord>> getAuditRecordsByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(auditService.getAuditRecordsByOrderNumber(orderNumber));
    }
}
