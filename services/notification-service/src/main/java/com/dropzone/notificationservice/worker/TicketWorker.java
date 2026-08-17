package com.dropzone.notificationservice.worker;

import com.dropzone.notificationservice.config.RabbitMQConfig;
import com.dropzone.notificationservice.model.JobPayload;
import com.dropzone.notificationservice.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketWorker {

    private final JobService jobService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TICKET_GENERATION)
    public void processTicketGenerationJob(JobPayload job) {
        log.info("[Ticket Worker] Consumed job from RabbitMQ queue '{}': JobId={}, Order={}",
                RabbitMQConfig.QUEUE_TICKET_GENERATION, job.getJobId(), job.getOrderNumber());

        try {
            // Simulate PDF / QR code ticket generation work
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }

        jobService.recordCompletedJob(job);
        log.info("[Ticket Worker] Successfully generated ticket for Order {}", job.getOrderNumber());
    }
}
