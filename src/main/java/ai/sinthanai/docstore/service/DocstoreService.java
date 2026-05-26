package ai.sinthanai.docstore.service;

import ai.sinthanai.docstore.domain.*;
import ai.sinthanai.docstore.exception.ChunkNotFoundException;
import ai.sinthanai.docstore.exception.DocumentNotFoundException;
import ai.sinthanai.docstore.infrastructure.BlobStorage;
import ai.sinthanai.docstore.repository.ChunkRepository;
import ai.sinthanai.docstore.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocstoreService {

    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;
    private final BlobStorage blobStorage;

    // -------------------------------------------------------------------------
    // Document operations
    // -------------------------------------------------------------------------

    @Transactional
    public Document registerDocument(String externalId, String source, String contentType) {
        log.debug("Registering document externalId={}", externalId);
        Document document = Document.builder()
            .externalId(externalId)
            .source(source)
            .contentType(contentType)
            .status(DocumentStatus.PENDING)
            .build();
        return documentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public Document getDocument(UUID id) {
        return documentRepository.findById(id)
            .orElseThrow(() -> new DocumentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Document getDocumentByExternalId(String externalId) {
        return documentRepository.findByExternalId(externalId)
            .orElseThrow(() -> new DocumentNotFoundException(externalId));
    }

    @Transactional
    public Document updateDocumentStatus(UUID id, DocumentStatus status, String errorMessage) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new DocumentNotFoundException(id));
        document.setStatus(status);
        document.setErrorMessage(errorMessage);
        return documentRepository.save(document);
    }

    // -------------------------------------------------------------------------
    // Chunk operations
    // -------------------------------------------------------------------------

    @Transactional
    public ChunkWithText storeChunk(
        UUID documentId,
        int chunkIndex,
        String text,
        int tokenCount,
        Map<String, String> metadata
    ) {
        if (!documentRepository.existsById(documentId)) {
            throw new DocumentNotFoundException(documentId);
        }

        String blobPath = blobPathFor(documentId, chunkIndex);
        blobStorage.put(blobPath, text.getBytes(StandardCharsets.UTF_8));

        Chunk chunk = Chunk.builder()
            .documentId(documentId)
            .chunkIndex(chunkIndex)
            .blobPath(blobPath)
            .tokenCount(tokenCount)
            .metadata(metadata.isEmpty() ? null : metadata)
            .build();
        chunk = chunkRepository.save(chunk);

        documentRepository.incrementChunkCount(documentId);
        log.debug("Stored chunk doc={} index={} id={}", documentId, chunkIndex, chunk.getId());

        return new ChunkWithText(chunk, text);
    }

    @Transactional(readOnly = true)
    public ChunkWithText getChunk(UUID id) {
        Chunk chunk = chunkRepository.findById(id)
            .orElseThrow(() -> new ChunkNotFoundException(id));
        String text = new String(blobStorage.get(chunk.getBlobPath()), StandardCharsets.UTF_8);
        return new ChunkWithText(chunk, text);
    }

    @Transactional(readOnly = true)
    public List<ChunkWithText> getChunksByDocument(UUID documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new DocumentNotFoundException(documentId);
        }
        return chunkRepository.findByDocumentIdOrderByChunkIndex(documentId)
            .stream()
            .map(chunk -> {
                String text = new String(blobStorage.get(chunk.getBlobPath()), StandardCharsets.UTF_8);
                return new ChunkWithText(chunk, text);
            })
            .toList();
    }

    @Transactional
    public int deleteChunksByDocument(UUID documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new DocumentNotFoundException(documentId);
        }
        List<Chunk> chunks = chunkRepository.findByDocumentIdOrderByChunkIndex(documentId);
        chunks.forEach(c -> blobStorage.delete(c.getBlobPath()));
        int deleted = chunkRepository.deleteByDocumentId(documentId);
        documentRepository.resetChunkCount(documentId);
        log.debug("Deleted {} chunks for doc={}", deleted, documentId);
        return deleted;
    }

    // -------------------------------------------------------------------------

    private String blobPathFor(UUID documentId, int chunkIndex) {
        return String.format("chunks/%s/%06d.txt", documentId, chunkIndex);
    }
}
