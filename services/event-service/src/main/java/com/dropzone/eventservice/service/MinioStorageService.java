package com.dropzone.eventservice.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.url:http://localhost:9000}")
    private String minioUrl;

    @Value("${minio.buckets.event-images:event-images}")
    private String eventImagesBucket;

    @Value("${minio.buckets.ticket-pdfs:ticket-pdfs}")
    private String ticketPdfsBucket;

    @Value("${minio.buckets.qr-tickets:qr-tickets}")
    private String qrTicketsBucket;

    @Value("${minio.buckets.uploads:uploads}")
    private String uploadsBucket;

    public String uploadMultipartFile(String bucketName, MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String objectName = UUID.randomUUID().toString() + extension;
            return uploadInputStream(bucketName, objectName, file.getInputStream(), file.getContentType(), file.getSize());
        } catch (Exception e) {
            log.error("Failed to upload multipart file to MinIO bucket {}", bucketName, e);
            throw new RuntimeException("MinIO upload failed: " + e.getMessage(), e);
        }
    }

    public String uploadInputStream(String bucketName, String objectName, InputStream inputStream, String contentType, long size) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType != null ? contentType : "application/octet-stream")
                            .build()
            );
            String url = String.format("%s/%s/%s", minioUrl, bucketName, objectName);
            log.info("Successfully uploaded object {} to bucket {}, URL: {}", objectName, bucketName, url);
            return url;
        } catch (Exception e) {
            log.error("Failed to upload stream to MinIO bucket {}", bucketName, e);
            throw new RuntimeException("MinIO upload failed: " + e.getMessage(), e);
        }
    }

    public InputStream downloadFile(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to download file {} from MinIO bucket {}", objectName, bucketName, e);
            throw new RuntimeException("MinIO download failed: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("Successfully deleted object {} from bucket {}", objectName, bucketName);
        } catch (Exception e) {
            log.error("Failed to delete file {} from MinIO bucket {}", objectName, bucketName, e);
            throw new RuntimeException("MinIO delete failed: " + e.getMessage(), e);
        }
    }

    public String getEventImagesBucket() {
        return eventImagesBucket;
    }

    public String getTicketPdfsBucket() {
        return ticketPdfsBucket;
    }

    public String getQrTicketsBucket() {
        return qrTicketsBucket;
    }

    public String getUploadsBucket() {
        return uploadsBucket;
    }
}
