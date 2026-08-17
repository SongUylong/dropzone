package com.dropzone.notificationservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "job_records")
public class JobRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false, length = 100)
    private String jobId;

    @Column(name = "job_type", nullable = false, length = 50)
    private String jobType;

    @Column(name = "target_queue", length = 100)
    private String targetQueue;

    @Column(name = "order_number", length = 100)
    private String orderNumber;

    @Column(name = "user_id")
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;
}
