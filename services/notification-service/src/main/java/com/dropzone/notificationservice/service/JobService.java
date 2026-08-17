package com.dropzone.notificationservice.service;

import com.dropzone.notificationservice.config.RabbitMQConfig;
import com.dropzone.notificationservice.model.JobPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final RabbitTemplate rabbitTemplate;
    private final List<JobPayload> jobHistory = Collections.synchronizedList(new ArrayList<>());

    public void dispatchJobsForOrderConfirmed(String orderNumber, String userId) {
        log.info("Kafka OrderConfirmed received -> Dispatching worker jobs to RabbitMQ for Order {}, User {}", orderNumber, userId);

        // 1. Dispatch Ticket Generation Job
        JobPayload ticketJob = JobPayload.builder()
                .jobId("JOB_TICKET_" + UUID.randomUUID().toString().substring(0, 8))
                .jobType("TICKET_GENERATION")
                .targetQueue(RabbitMQConfig.QUEUE_TICKET_GENERATION)
                .orderNumber(orderNumber)
                .userId(userId)
                .details("Generate PDF & QR Code ticket for order " + orderNumber)
                .status("PENDING")
                .createdAt(Instant.now())
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_JOBS, RabbitMQConfig.ROUTING_KEY_TICKET, ticketJob);
        log.info("Sent job to RabbitMQ queue '{}': {}", RabbitMQConfig.QUEUE_TICKET_GENERATION, ticketJob.getJobId());

        // 2. Dispatch Email Job
        JobPayload emailJob = JobPayload.builder()
                .jobId("JOB_EMAIL_" + UUID.randomUUID().toString().substring(0, 8))
                .jobType("EMAIL_NOTIFICATION")
                .targetQueue(RabbitMQConfig.QUEUE_EMAIL)
                .orderNumber(orderNumber)
                .userId(userId)
                .details("Send order confirmation email to User " + userId)
                .status("PENDING")
                .createdAt(Instant.now())
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_JOBS, RabbitMQConfig.ROUTING_KEY_EMAIL, emailJob);
        log.info("Sent job to RabbitMQ queue '{}': {}", RabbitMQConfig.QUEUE_EMAIL, emailJob.getJobId());

        // 3. Dispatch SMS Job
        JobPayload smsJob = JobPayload.builder()
                .jobId("JOB_SMS_" + UUID.randomUUID().toString().substring(0, 8))
                .jobType("SMS_NOTIFICATION")
                .targetQueue(RabbitMQConfig.QUEUE_SMS)
                .orderNumber(orderNumber)
                .userId(userId)
                .details("Send SMS confirmation alert to User " + userId)
                .status("PENDING")
                .createdAt(Instant.now())
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_JOBS, RabbitMQConfig.ROUTING_KEY_SMS, smsJob);
        log.info("Sent job to RabbitMQ queue '{}': {}", RabbitMQConfig.QUEUE_SMS, smsJob.getJobId());
    }

    public void recordCompletedJob(JobPayload job) {
        job.setStatus("COMPLETED");
        job.setProcessedAt(Instant.now());
        jobHistory.add(job);
        log.info("Job {} on queue '{}' COMPLETED by worker!", job.getJobId(), job.getTargetQueue());
    }

    public List<JobPayload> getAllJobs() {
        return new ArrayList<>(jobHistory);
    }

    public List<JobPayload> getJobsByQueue(String queueName) {
        return jobHistory.stream()
                .filter(j -> j.getTargetQueue() != null && j.getTargetQueue().equalsIgnoreCase(queueName))
                .collect(Collectors.toList());
    }
}
