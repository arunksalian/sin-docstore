package ai.sinthanai.docstore.infrastructure;

import ai.sinthanai.docstore.config.StorageProperties;
import ai.sinthanai.docstore.exception.BlobStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
@Profile("local")
public class LocalFileStorageAdapter implements BlobStorage {

    private final Path rootPath;

    public LocalFileStorageAdapter(StorageProperties properties) {
        this.rootPath = Path.of(properties.getLocal().getRootPath());
        log.info("LocalFileStorageAdapter initialised at {}", rootPath);
    }

    @Override
    public String put(String path, byte[] data) {
        Path target = rootPath.resolve(path);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, data);
        } catch (IOException e) {
            throw new BlobStorageException("Failed to write blob: " + path, e);
        }
        return path;
    }

    @Override
    public byte[] get(String path) {
        try {
            return Files.readAllBytes(rootPath.resolve(path));
        } catch (IOException e) {
            throw new BlobStorageException("Failed to read blob: " + path, e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            Files.deleteIfExists(rootPath.resolve(path));
        } catch (IOException e) {
            throw new BlobStorageException("Failed to delete blob: " + path, e);
        }
    }

    @Override
    public boolean exists(String path) {
        return Files.exists(rootPath.resolve(path));
    }
}
