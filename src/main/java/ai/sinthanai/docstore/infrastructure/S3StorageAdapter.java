package ai.sinthanai.docstore.infrastructure;

import ai.sinthanai.docstore.exception.BlobStorageException;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

// Not a @Component — registered by S3Config so constructor args wire correctly.
@Slf4j
public class S3StorageAdapter implements BlobStorage {

    private final S3Client s3Client;
    private final String bucket;

    public S3StorageAdapter(S3Client s3Client, String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        log.info("S3StorageAdapter initialised for bucket '{}'", bucket);
    }

    @Override
    public String put(String path, byte[] data) {
        try {
            s3Client.putObject(
                PutObjectRequest.builder().bucket(bucket).key(path).build(),
                RequestBody.fromBytes(data)
            );
        } catch (SdkClientException e) {
            throw new BlobStorageException("Failed to write blob to S3: " + path, e);
        }
        return path;
    }

    @Override
    public byte[] get(String path) {
        try {
            return s3Client.getObjectAsBytes(
                GetObjectRequest.builder().bucket(bucket).key(path).build()
            ).asByteArray();
        } catch (NoSuchKeyException e) {
            throw new BlobStorageException("Blob not found in S3: " + path, e);
        } catch (SdkClientException e) {
            throw new BlobStorageException("Failed to read blob from S3: " + path, e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            s3Client.deleteObject(
                DeleteObjectRequest.builder().bucket(bucket).key(path).build()
            );
        } catch (SdkClientException e) {
            throw new BlobStorageException("Failed to delete blob from S3: " + path, e);
        }
    }

    @Override
    public boolean exists(String path) {
        try {
            s3Client.headObject(
                HeadObjectRequest.builder().bucket(bucket).key(path).build()
            );
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (SdkClientException e) {
            throw new BlobStorageException("Failed to check blob existence in S3: " + path, e);
        }
    }
}
