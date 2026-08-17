package com.dropzone.notificationservice.worker;

import com.dropzone.notificationservice.config.RabbitMQConfig;
import com.dropzone.notificationservice.model.JobPayload;
import com.dropzone.notificationservice.model.TicketRecord;
import com.dropzone.notificationservice.service.JobService;
import com.dropzone.notificationservice.service.MinioStorageService;
import com.dropzone.notificationservice.service.PdfTicketGeneratorService;
import com.dropzone.notificationservice.service.QrCodeGeneratorService;
import com.dropzone.notificationservice.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketWorker {

    private final JobService jobService;
    private final QrCodeGeneratorService qrCodeGeneratorService;
    private final PdfTicketGeneratorService pdfTicketGeneratorService;
    private final MinioStorageService minioStorageService;
    private final TicketService ticketService;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TICKET_GENERATION)
    public void processTicketGenerationJob(JobPayload job) {
        log.info("[Ticket Worker] Consumed job from RabbitMQ queue '{}': JobId={}, Order={}",
                RabbitMQConfig.QUEUE_TICKET_GENERATION, job.getJobId(), job.getOrderNumber());

        try {
            // Chaos Lab Check
            if (redisTemplate != null) {
                Boolean slow = "true".equalsIgnoreCase(redisTemplate.opsForValue().get("chaos:notification:slow_worker"));
                Boolean reject = "true".equalsIgnoreCase(redisTemplate.opsForValue().get("chaos:notification:reject_messages"));
                if (slow != null && slow) {
                    log.warn("Chaos Lab: TicketWorker delaying processing by 5000ms...");
                    Thread.sleep(5000);
                }
                if (reject != null && reject) {
                    log.warn("Chaos Lab: TicketWorker rejecting job {}", job.getJobId());
                    throw new RuntimeException("Chaos Lab: Injected message rejection");
                }
            }

            String orderNumber = job.getOrderNumber() != null ? job.getOrderNumber() : "DZ10239";
            
            // Format Ticket ID as DZ-928231 or derived from order
            String ticketId = "DZ-" + Math.abs(orderNumber.hashCode() % 900000 + 100000);
            String eventName = "Coldplay Concert";
            String categoryName = "VIP";
            String seatNumber = "Seat A102";
            String eventDate = "October 10";

            // 1. Generate QR Code PNG
            String qrPayload = String.format("{\"ticketId\":\"%s\",\"orderNumber\":\"%s\",\"event\":\"%s\",\"seat\":\"%s\"}",
                    ticketId, orderNumber, eventName, seatNumber);
            byte[] qrCodeBytes = qrCodeGeneratorService.generateQrCodePng(qrPayload, 250, 250);

            // 2. Upload QR Code to MinIO (qr-tickets bucket)
            String qrFilename = String.format("qr_%s_%s.png", ticketId, orderNumber);
            String qrCodeUrl = minioStorageService.uploadQrCode(qrFilename, qrCodeBytes);

            // 3. Generate PDF Ticket with embedded QR Code image
            byte[] pdfBytes = pdfTicketGeneratorService.generateTicketPdf(
                    eventName, categoryName, seatNumber, eventDate, ticketId, qrCodeBytes
            );

            // 4. Upload PDF Ticket to MinIO (ticket-pdfs bucket)
            String pdfFilename = String.format("ticket_%s_%s.pdf", ticketId, orderNumber);
            String pdfUrl = minioStorageService.uploadTicketPdf(pdfFilename, pdfBytes);

            // 5. Store Ticket Record
            TicketRecord ticketRecord = TicketRecord.builder()
                    .ticketId(ticketId)
                    .orderNumber(orderNumber)
                    .userId(job.getUserId())
                    .eventName(eventName)
                    .categoryName(categoryName)
                    .seatNumber(seatNumber)
                    .eventDate(eventDate)
                    .qrCodeUrl(qrCodeUrl)
                    .pdfUrl(pdfUrl)
                    .createdAt(Instant.now())
                    .build();
            ticketService.recordTicket(ticketRecord);

            // 6. Record Job Completion
            jobService.recordCompletedJob(job);
            log.info("[Ticket Worker] Successfully generated and uploaded ticket! TicketId={}, PDF URL={}, QR URL={}",
                    ticketId, pdfUrl, qrCodeUrl);

        } catch (Exception e) {
            log.error("[Ticket Worker] Error generating ticket for JobId={}: {}", job.getJobId(), e.getMessage(), e);
        }
    }
}
