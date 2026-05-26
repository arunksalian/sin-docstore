package ai.sinthanai.docstore.config;

import ai.sinthanai.docstore.infrastructure.S3StorageAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@Profile({"docker", "prod"})
public class S3Config {

    @Bean
    public S3Client s3Client(StorageProperties properties) {
        StorageProperties.S3 s3 = properties.getS3();
        var builder = S3Client.builder()
            .region(Region.of(s3.getRegion()))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey())
            ));

        if (s3.getEndpoint() != null && !s3.getEndpoint().isBlank()) {
            // MinIO / localstack endpoint override
            builder.endpointOverride(URI.create(s3.getEndpoint()))
                   .forcePathStyle(true);
        }

        return builder.build();
    }

    @Bean
    public String s3BucketName(StorageProperties properties) {
        return properties.getS3().getBucket();
    }

    @Bean
    public S3StorageAdapter s3StorageAdapter(S3Client s3Client, String s3BucketName) {
        return new S3StorageAdapter(s3Client, s3BucketName);
    }
}
