package com.logicsoft.yvideogen.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Configuration for AWS S3 client
 * Only creates the S3Client bean if aws.s3.enabled=true
 * Properly configures the region for bucket access
 */
@Slf4j
@Configuration
public class AwsS3Config {

    @Value("${aws.s3.region:us-east-1}")
    private String awsRegion;

    @Bean
    @ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
    public S3Client s3Client() {
        log.info("Initializing AWS S3 Client with region: {}", awsRegion);
        try {
            Region region = Region.of(awsRegion);
            return S3Client.builder()
                    .region(region)
                    .build();
        } catch (Exception e) {
            log.error("Failed to initialize S3 client with region: {}", awsRegion, e);
            log.warn("Falling back to default region configuration");
            return S3Client.builder().build();
        }
    }
}

