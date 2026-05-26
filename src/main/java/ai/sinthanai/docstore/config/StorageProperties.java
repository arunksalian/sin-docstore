package ai.sinthanai.docstore.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "docstore.storage")
public class StorageProperties {

    private Local local = new Local();
    private S3 s3 = new S3();

    @Getter
    @Setter
    public static class Local {
        private String rootPath = "/tmp/sin-docstore/blobs";
    }

    @Getter
    @Setter
    public static class S3 {
        private String endpoint;
        private String bucket = "sin-docstore";
        private String region = "us-east-1";
        private String accessKey;
        private String secretKey;
    }
}
