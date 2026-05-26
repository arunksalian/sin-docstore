package ai.sinthanai.docstore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "docstore.storage")
public class StorageProperties {

    private Local local = new Local();
    private S3 s3 = new S3();

    public Local getLocal() { return local; }
    public void setLocal(Local local) { this.local = local; }

    public S3 getS3() { return s3; }
    public void setS3(S3 s3) { this.s3 = s3; }

    public static class Local {
        private String rootPath = "/tmp/sin-docstore/blobs";

        public String getRootPath() { return rootPath; }
        public void setRootPath(String rootPath) { this.rootPath = rootPath; }
    }

    public static class S3 {
        private String endpoint;
        private String bucket = "sin-docstore";
        private String region = "us-east-1";
        private String accessKey;
        private String secretKey;

        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }

        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }

        public String getAccessKey() { return accessKey; }
        public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    }
}
