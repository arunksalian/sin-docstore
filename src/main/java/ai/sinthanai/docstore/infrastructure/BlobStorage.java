package ai.sinthanai.docstore.infrastructure;

/**
 * Storage abstraction for chunk text blobs.
 *
 * Two implementations:
 *  - {@link LocalFileStorageAdapter} — local filesystem (profile: local)
 *  - {@link S3StorageAdapter}        — S3 / MinIO (profiles: docker, prod)
 *
 * Paths are always relative (e.g. "chunks/{docId}/{index}.txt").
 * Each implementation resolves them against its configured root.
 */
public interface BlobStorage {

    /**
     * Write {@code data} to {@code path}. Creates parent directories as needed.
     *
     * @return the path that was stored (same as input, for convenience)
     */
    String put(String path, byte[] data);

    /**
     * Read all bytes at {@code path}.
     */
    byte[] get(String path);

    /**
     * Delete the blob at {@code path}. No-op if the blob does not exist.
     */
    void delete(String path);

    /**
     * Return {@code true} if a blob exists at {@code path}.
     */
    boolean exists(String path);
}
