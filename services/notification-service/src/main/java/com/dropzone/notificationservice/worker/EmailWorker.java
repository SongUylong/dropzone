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
public class EmailWorker {

    private final JobService jobService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
    public void processEmailJob(JobPayload job) {
        log.info("[Email Worker] Consumed job from RabbitMQ queue '{}': JobId={}, Order={}, User={}",
                RabbitMQConfig.QUEUE_EMAIL, job.getJobId(), job.getOrderNumber(), job.getUserId());

        try {
            // Simulate sending email confirmation
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }

        jobService.recordCompletedJob(job);
        log.info("[Email Worker] Successfully sent email notification for Order {} to User {}", job.getOrderNumber(), job.getUserId());
    }
}
