package com.logicsoft.yvideogen.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.core.sync.RequestBody;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * S3 Storage implementation of StorageService
 * Stores generated scenes and content in AWS S3
 * Only active when aws.s3.enabled=true and S3Client bean is available
 */
@Slf4j
@Service
@ConditionalOnBean(S3Client.class)
public class S3StorageService implements StorageService {

    @Value("${aws.s3.bucket-name:}")
    private String bucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    private final Optional<S3Client> s3Client;

    public S3StorageService(Optional<S3Client> s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public UploadResult uploadContent(String content, String fileName) throws Exception {
        if (!s3Enabled || !s3Client.isPresent()) {
            log.warn("S3 storage is not enabled or S3Client is not available. Configure 'aws.s3.enabled=true' to use S3 storage.");
            throw new RuntimeException("S3 storage is not enabled or S3Client is not available. Configure 'aws.s3.enabled=true' to use S3 storage.");
        }

        try {
            log.info("Uploading content to S3 bucket: {} in region: {} with filename: {}", bucketName, region, fileName);

            // Validate bucket is configured
            if (bucketName == null || bucketName.isEmpty()) {
                throw new RuntimeException("S3 bucket name not configured. Set 'aws.s3.bucket-name' property.");
            }

            // Validate region is configured
            if (region == null || region.isEmpty()) {
                log.warn("AWS region not explicitly configured. Using default (usually us-east-1). If your bucket is in a different region, set 'aws.s3.region' property.");
            }

            // Upload to S3
            String key = "scenes/" + fileName;
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("text/plain")
                    .build();

            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            PutObjectResponse response = s3Client.get().putObject(putObjectRequest, 
                    RequestBody.fromBytes(contentBytes));

            String fileUrl = String.format("s3://%s/%s", bucketName, key);
            
            log.info("Successfully uploaded file to S3. URL: {}", fileUrl);

            return UploadResult.builder()
                    .fileId(response.eTag())
                    .fileLink(fileUrl)
                    .fileName(fileName)
                    .storageType("S3")
                    .build();

        } catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
            log.error("S3 service error: {} - Status Code: {}", e.awsErrorDetails().errorMessage(), e.statusCode(), e);
            if (e.statusCode() == 301 || e.awsErrorDetails().errorMessage().contains("endpoint")) {
                throw new RuntimeException(
                    "S3 endpoint error: The bucket may be in a different region than configured. " +
                    "Set 'aws.s3.region' to the correct region (e.g., us-west-2, eu-west-1). " +
                    "Error: " + e.awsErrorDetails().errorMessage(), e);
            }
            throw new RuntimeException("Failed to upload to S3: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error uploading content to S3", e);
            throw new RuntimeException("Failed to upload to S3: " + e.getMessage(), e);
        }
    }

    @Override
    public UploadResult uploadScenes(String theme, List<String> scenes) {
        if (!s3Enabled || !s3Client.isPresent()) {
            log.warn("S3 storage is not enabled or S3Client is not available. Configure 'aws.s3.enabled=true' to use S3 storage.");
            throw new RuntimeException("S3 storage is not enabled or S3Client is not available. Configure 'aws.s3.enabled=true' to use S3 storage.");
        }

        try {
            log.info("Uploading scenes to S3 for theme: {} in region: {}", theme, region);

            // Build content
            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append("Video Scenes for Theme: ").append(theme).append("\n");
            contentBuilder.append("Generated on: ").append(new java.util.Date()).append("\n");
            contentBuilder.append("=".repeat(50)).append("\n\n");

            for (int i = 0; i < scenes.size(); i++) {
                contentBuilder.append("Scene ").append(i + 1).append(":\n");
                contentBuilder.append(scenes.get(i)).append("\n\n");
            }

            // Generate filename
            String fileName = "video_scenes_" + theme.replaceAll("[^a-zA-Z0-9]", "_") + ".txt";

            // Upload using the general upload content method
            return uploadContent(contentBuilder.toString(), fileName);

        } catch (Exception e) {
            log.error("Error uploading scenes to S3", e);
            throw new RuntimeException("Failed to upload scenes to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Check if S3 storage is properly configured
     */
    public boolean isConfigured() {
        return s3Enabled && bucketName != null && !bucketName.isEmpty();
    }
}

