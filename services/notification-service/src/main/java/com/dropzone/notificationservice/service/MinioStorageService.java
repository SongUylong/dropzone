package com.dropzone.notificationservice.service;

import io.minio.PutObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.url:http://localhost:9000}")
    private String minioUrl;

    @Value("${minio.buckets.qr-tickets:qr-tickets}")
    private String qrTicketsBucket;

    @Value("${minio.buckets.ticket-pdfs:ticket-pdfs}")
    private String ticketPdfsBucket;

    public String uploadQrCode(String filename, byte[] bytes) throws Exception {
        return uploadFile(qrTicketsBucket, filename, bytes, "image/png");
    }

    public String uploadTicketPdf(String filename, byte[] bytes) throws Exception {
        return uploadFile(ticketPdfsBucket, filename, bytes, "application/pdf");
    }

    private String uploadFile(String bucket, String filename, byte[] bytes, String contentType) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(filename)
                        .stream(inputStream, bytes.length, -1)
                        .contentType(contentType)
                        .build()
        );
        String url = String.format("%s/%s/%s", minioUrl, bucket, filename);
        log.info("[MinIO Storage] Uploaded {} bytes to bucket '{}' as '{}': {}", bytes.length, bucket, filename, url);
        return url;
    }
}
