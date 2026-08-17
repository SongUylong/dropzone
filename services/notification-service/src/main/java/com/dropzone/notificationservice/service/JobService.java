package com.dropzone.notificationservice.service;

import com.dropzone.notificationservice.config.RabbitMQConfig;
import com.dropzone.notificationservice.model.JobPayload;
import com.dropzone.notificationservice.model.JobRecord;
import com.dropzone.notificationservice.repository.JobRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final RabbitTemplate rabbitTemplate;
    private final JobRecordRepository jobRecordRepository;

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

    @Transactional
    public void recordCompletedJob(JobPayload job) {
        JobRecord record = JobRecord.builder()
                .jobId(job.getJobId())
                .jobType(job.getJobType())
                .targetQueue(job.getTargetQueue())
                .orderNumber(job.getOrderNumber())
                .userId(job.getUserId())
                .details(job.getDetails())
                .status("COMPLETED")
                .createdAt(job.getCreatedAt())
                .processedAt(Instant.now())
                .build();
        jobRecordRepository.save(record);
        log.info("Job {} on queue '{}' COMPLETED and persisted to database!", job.getJobId(), job.getTargetQueue());
    }

    public List<JobRecord> getAllJobs() {
        return jobRecordRepository.findAll();
    }

    public List<JobRecord> getJobsByQueue(String queueName) {
        return jobRecordRepository.findByTargetQueueIgnoreCase(queueName);
    }
}
