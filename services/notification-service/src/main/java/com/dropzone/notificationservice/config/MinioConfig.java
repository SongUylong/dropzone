package com.dropzone.notificationservice.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
public class MinioConfig {

    @Value("${minio.url:http://localhost:9000}")
    private String minioUrl;

    @Value("${minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${minio.secret-key:minioadmin}")
    private String secretKey;

    @Value("${minio.buckets.qr-tickets:qr-tickets}")
    private String qrTicketsBucket;

    @Value("${minio.buckets.ticket-pdfs:ticket-pdfs}")
    private String ticketPdfsBucket;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean
    public CommandLineRunner initializeMinioNotificationBuckets(MinioClient minioClient) {
        return args -> {
            List<String> buckets = List.of(qrTicketsBucket, ticketPdfsBucket);
            for (String bucket : buckets) {
                try {
                    boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
                    if (!found) {
                        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                        log.info("[MinIO Config] Created bucket: {}", bucket);

                        String policy = """
                                {
                                  "Version": "2012-10-17",
                                  "Statement": [
                                    {
                                      "Effect": "Allow",
                                      "Principal": {"AWS": ["*"]},
                                      "Action": ["s3:GetObject"],
                                      "Resource": ["arn:aws:s3:::%s/*"]
                                    }
                                  ]
                                }
                                """.formatted(bucket);
                        try {
                            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucket).config(policy).build());
                        } catch (Exception e) {
                            log.warn("[MinIO Config] Could not set public policy on bucket {}: {}", bucket, e.getMessage());
                        }
                    } else {
                        log.info("[MinIO Config] Bucket already exists: {}", bucket);
                    }
                } catch (Exception e) {
                    log.error("[MinIO Config] Failed to initialize bucket {}: {}", bucket, e.getMessage());
                }
            }
        };
    }
}
