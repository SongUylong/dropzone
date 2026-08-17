package com.dropzone.notificationservice.controller;

import com.dropzone.notificationservice.model.JobPayload;
import com.dropzone.notificationservice.model.NotificationRecord;
import com.dropzone.notificationservice.service.JobService;
import com.dropzone.notificationservice.service.NotificationService;
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
    public ResponseEntity<List<JobPayload>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/jobs/queue/{queueName}")
    public ResponseEntity<List<JobPayload>> getJobsByQueue(@PathVariable String queueName) {
        return ResponseEntity.ok(jobService.getJobsByQueue(queueName));
    }
}
