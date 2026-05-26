package ai.sinthanai.docstore.exception;

import java.util.UUID;

public class ChunkNotFoundException extends RuntimeException {

    public ChunkNotFoundException(UUID id) {
        super("Chunk not found: " + id);
    }
}
