package com.sparta.server.threeserving.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsS3Config {

    @Value("${cloud.aws.region}")
    private String region;

    // LocalStack 등 로컬 S3 엔드포인트. 비어 있으면 실제 AWS 사용
    @Value("${cloud.aws.s3.endpoint:}")
    private String endpoint;

    @Value("${cloud.aws.credentials.access-key:}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key:}")
    private String secretKey;

    private boolean isLocal() {
        return endpoint != null && !endpoint.isBlank();
    }

    private AwsCredentialsProvider credentialsProvider() {
        if (isLocal()) {
            // 로컬은 더미 정적 키 (test/test)
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                            accessKey.isBlank() ? "test" : accessKey,
                            secretKey.isBlank() ? "test" : secretKey));
        }
        // 운영은 IAM Role 등 기본 자격증명 체인
        return DefaultCredentialsProvider.create();
    }

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider());
        if (isLocal()) {
            builder.endpointOverride(URI.create(endpoint))
                    .forcePathStyle(true);   // LocalStack 은 path-style 필요
        }
        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        var builder = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider());
        if (isLocal()) {
            builder.endpointOverride(URI.create(endpoint))
                    .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }
        return builder.build();
    }

    // 로컬(LocalStack)일 때만: 버킷 자동 생성 + 브라우저 직접 PUT 용 CORS 설정
    @Bean
    public ApplicationRunner localS3Init(S3Client s3Client,
                                         @Value("${cloud.aws.s3.bucket}") String bucket) {
        return args -> {
            if (!isLocal()) return;
            try {
                s3Client.createBucket(b -> b.bucket(bucket));
            } catch (Exception ignore) {
                // 이미 존재하면 무시
            }
            try {
                s3Client.putBucketCors(b -> b.bucket(bucket).corsConfiguration(c -> c.corsRules(
                        CORSRule.builder()
                                .allowedMethods("PUT", "GET")
                                .allowedOrigins("*")
                                .allowedHeaders("*")
                                .build())));
            } catch (Exception ignore) {
            }
        };
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
