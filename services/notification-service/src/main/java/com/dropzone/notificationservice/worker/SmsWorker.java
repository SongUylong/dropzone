package com.dropzone.notificationservice.worker;

import com.dropzone.notificationservice.config.RabbitMQConfig;
import com.dropzone.notificationservice.model.JobPayload;
import com.dropzone.notificationservice.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsWorker {

    private final JobService jobService;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_SMS)
    public void processSmsJob(JobPayload job) {
        log.info("[SMS Worker] Consumed job from RabbitMQ queue '{}': JobId={}, Order={}, User={}",
                RabbitMQConfig.QUEUE_SMS, job.getJobId(), job.getOrderNumber(), job.getUserId());

        try {
            // Chaos Lab Check
            if (redisTemplate != null) {
                Boolean slow = "true".equalsIgnoreCase(redisTemplate.opsForValue().get("chaos:notification:slow_worker"));
                Boolean reject = "true".equalsIgnoreCase(redisTemplate.opsForValue().get("chaos:notification:reject_messages"));
                if (slow != null && slow) {
                    log.warn("Chaos Lab: SmsWorker delaying processing by 5000ms...");
                    Thread.sleep(5000);
                }
                if (reject != null && reject) {
                    log.warn("Chaos Lab: SmsWorker rejecting job {}", job.getJobId());
                    throw new RuntimeException("Chaos Lab: Injected message rejection");
                }
            }

            // Simulate sending SMS alert
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }

        jobService.recordCompletedJob(job);
        log.info("[SMS Worker] Successfully delivered SMS alert for Order {} to User {}", job.getOrderNumber(), job.getUserId());
    }
}
