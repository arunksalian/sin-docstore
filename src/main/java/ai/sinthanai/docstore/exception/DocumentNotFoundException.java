package ai.sinthanai.docstore.exception;

import java.util.UUID;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(UUID id) {
        super("Document not found: " + id);
    }

    public DocumentNotFoundException(String externalId) {
        super("Document not found with external_id: " + externalId);
    }
}
