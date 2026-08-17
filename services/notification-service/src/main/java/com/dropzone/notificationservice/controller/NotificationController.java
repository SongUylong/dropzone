package com.dropzone.notificationservice.controller;

import com.dropzone.notificationservice.model.JobRecord;
import com.dropzone.notificationservice.model.NotificationRecord;
import com.dropzone.notificationservice.model.TicketRecord;
import com.dropzone.notificationservice.service.JobService;
import com.dropzone.notificationservice.service.NotificationService;
import com.dropzone.notificationservice.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JobService jobService;
    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<NotificationRecord>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/order/{orderNumber}")
    public ResponseEntity<List<NotificationRecord>> getNotificationsByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(notificationService.getNotificationsByOrderNumber(orderNumber));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationRecord>> getNotificationsByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId));
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<JobRecord>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/jobs/queue/{queueName}")
    public ResponseEntity<List<JobRecord>> getJobsByQueue(@PathVariable String queueName) {
        return ResponseEntity.ok(jobService.getJobsByQueue(queueName));
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<TicketRecord>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/tickets/order/{orderNumber}")
    public ResponseEntity<TicketRecord> getTicketByOrderNumber(@PathVariable String orderNumber) {
        return ticketService.getTicketByOrderNumber(orderNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<TicketRecord> getTicketById(@PathVariable String ticketId) {
        return ticketService.getTicketById(ticketId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
