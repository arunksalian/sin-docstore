package ai.sinthanai.docstore.mapper;

import ai.sinthanai.docstore.domain.Chunk;
import ai.sinthanai.docstore.domain.ChunkWithText;
import ai.sinthanai.docstore.domain.Document;
import ai.sinthanai.docstore.domain.DocumentStatus;
import ai.sinthanai.docstore.grpc.proto.*;
import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class ProtoMapper {

    // -------------------------------------------------------------------------
    // Timestamp
    // -------------------------------------------------------------------------

    public Timestamp toProto(Instant instant) {
        if (instant == null) return Timestamp.getDefaultInstance();
        return Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();
    }

    // -------------------------------------------------------------------------
    // DocumentStatus
    // -------------------------------------------------------------------------

    public ai.sinthanai.docstore.grpc.proto.DocumentStatus toProto(DocumentStatus status) {
        if (status == null) return ai.sinthanai.docstore.grpc.proto.DocumentStatus.DOCUMENT_STATUS_UNSPECIFIED;
        return switch (status) {
            case PENDING    -> ai.sinthanai.docstore.grpc.proto.DocumentStatus.DOCUMENT_STATUS_PENDING;
            case PROCESSING -> ai.sinthanai.docstore.grpc.proto.DocumentStatus.DOCUMENT_STATUS_PROCESSING;
            case INDEXED    -> ai.sinthanai.docstore.grpc.proto.DocumentStatus.DOCUMENT_STATUS_INDEXED;
            case FAILED     -> ai.sinthanai.docstore.grpc.proto.DocumentStatus.DOCUMENT_STATUS_FAILED;
        };
    }

    public DocumentStatus fromProto(ai.sinthanai.docstore.grpc.proto.DocumentStatus status) {
        return switch (status) {
            case DOCUMENT_STATUS_PENDING    -> DocumentStatus.PENDING;
            case DOCUMENT_STATUS_PROCESSING -> DocumentStatus.PROCESSING;
            case DOCUMENT_STATUS_INDEXED    -> DocumentStatus.INDEXED;
            case DOCUMENT_STATUS_FAILED     -> DocumentStatus.FAILED;
            default -> throw new IllegalArgumentException("Unknown status: " + status);
        };
    }

    // -------------------------------------------------------------------------
    // Document
    // -------------------------------------------------------------------------

    public ai.sinthanai.docstore.grpc.proto.Document toProto(Document doc) {
        var builder = ai.sinthanai.docstore.grpc.proto.Document.newBuilder()
            .setId(doc.getId().toString())
            .setExternalId(doc.getExternalId())
            .setSource(doc.getSource())
            .setStatus(toProto(doc.getStatus()))
            .setChunkCount(doc.getChunkCount())
            .setCreatedAt(toProto(doc.getCreatedAt()))
            .setUpdatedAt(toProto(doc.getUpdatedAt()));

        if (doc.getContentType() != null) builder.setContentType(doc.getContentType());
        if (doc.getErrorMessage() != null) builder.setErrorMessage(doc.getErrorMessage());

        return builder.build();
    }

    // -------------------------------------------------------------------------
    // Chunk
    // -------------------------------------------------------------------------

    public ai.sinthanai.docstore.grpc.proto.Chunk toProto(ChunkWithText cwt) {
        Chunk chunk = cwt.chunk();
        var builder = ai.sinthanai.docstore.grpc.proto.Chunk.newBuilder()
            .setId(chunk.getId().toString())
            .setDocumentId(chunk.getDocumentId().toString())
            .setChunkIndex(chunk.getChunkIndex())
            .setText(cwt.text())
            .setTokenCount(chunk.getTokenCount())
            .setCreatedAt(toProto(chunk.getCreatedAt()));

        Map<String, String> meta = chunk.getMetadata();
        if (meta != null) builder.putAllMetadata(meta);

        return builder.build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    public UUID parseUuid(String id, String fieldName) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID for field '" + fieldName + "': " + id);
        }
    }
}
