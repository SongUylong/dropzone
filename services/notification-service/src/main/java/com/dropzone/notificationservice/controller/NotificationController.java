package com.dropzone.notificationservice.controller;

import com.dropzone.notificationservice.model.NotificationRecord;
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

    @GetMapping
    public ResponseEntity<List<NotificationRecord>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/order/{orderNumber}")
    public ResponseEntity<List<NotificationRecord>> getNotificationsByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(notificationService.getNotificationsByOrderNumber(orderNumber));
    }
}
