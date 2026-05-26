package ai.sinthanai.docstore.integration;

import ai.sinthanai.docstore.domain.ChunkWithText;
import ai.sinthanai.docstore.domain.Document;
import ai.sinthanai.docstore.domain.DocumentStatus;
import ai.sinthanai.docstore.exception.ChunkNotFoundException;
import ai.sinthanai.docstore.exception.DocumentNotFoundException;
import ai.sinthanai.docstore.service.DocstoreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@Testcontainers
class DocstoreIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("sinthanai_docstore")
        .withUsername("docstore")
        .withPassword("docstore");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws IOException {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        String tempDir = Files.createTempDirectory("sin-docstore-test").toString();
        registry.add("docstore.storage.local.root-path", () -> tempDir);
    }

    @Autowired
    DocstoreService docstoreService;

    @Test
    void registerDocument_persists_and_returns_document() {
        Document doc = docstoreService.registerDocument("ext-001", "file.pdf", "application/pdf");

        assertThat(doc.getId()).isNotNull();
        assertThat(doc.getExternalId()).isEqualTo("ext-001");
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.PENDING);
        assertThat(doc.getChunkCount()).isZero();
        assertThat(doc.getCreatedAt()).isNotNull();
    }

    @Test
    void getDocument_returnsById() {
        Document created = docstoreService.registerDocument("ext-002", "doc.txt", "text/plain");
        Document fetched = docstoreService.getDocument(created.getId());

        assertThat(fetched.getExternalId()).isEqualTo("ext-002");
    }

    @Test
    void getDocument_throwsWhenMissing() {
        assertThatThrownBy(() -> docstoreService.getDocument(UUID.randomUUID()))
            .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void getDocumentByExternalId_returnsDocument() {
        docstoreService.registerDocument("ext-lookup", "s3://bucket/key", null);
        Document fetched = docstoreService.getDocumentByExternalId("ext-lookup");

        assertThat(fetched.getSource()).isEqualTo("s3://bucket/key");
    }

    @Test
    void updateDocumentStatus_changesStatus() {
        Document doc = docstoreService.registerDocument("ext-003", "doc3.pdf", null);
        Document updated = docstoreService.updateDocumentStatus(doc.getId(), DocumentStatus.INDEXED, null);

        assertThat(updated.getStatus()).isEqualTo(DocumentStatus.INDEXED);
        assertThat(updated.getErrorMessage()).isNull();
    }

    @Test
    void updateDocumentStatus_toFailed_storesErrorMessage() {
        Document doc = docstoreService.registerDocument("ext-004", "doc4.pdf", null);
        Document updated = docstoreService.updateDocumentStatus(doc.getId(), DocumentStatus.FAILED, "parse error");

        assertThat(updated.getStatus()).isEqualTo(DocumentStatus.FAILED);
        assertThat(updated.getErrorMessage()).isEqualTo("parse error");
    }

    @Test
    void storeChunk_persistsAndBlobWritten() {
        Document doc = docstoreService.registerDocument("ext-005", "doc5.pdf", null);
        ChunkWithText cwt = docstoreService.storeChunk(doc.getId(), 0, "Hello world", 2, Map.of("page", "1"));

        assertThat(cwt.chunk().getId()).isNotNull();
        assertThat(cwt.chunk().getDocumentId()).isEqualTo(doc.getId());
        assertThat(cwt.chunk().getChunkIndex()).isZero();
        assertThat(cwt.text()).isEqualTo("Hello world");
        assertThat(cwt.chunk().getMetadata()).containsEntry("page", "1");

        // chunk_count is incremented
        Document refreshed = docstoreService.getDocument(doc.getId());
        assertThat(refreshed.getChunkCount()).isEqualTo(1);
    }

    @Test
    void getChunk_retrievesTextFromBlob() {
        Document doc = docstoreService.registerDocument("ext-006", "doc6.txt", null);
        ChunkWithText stored = docstoreService.storeChunk(doc.getId(), 0, "Retrieval text", 2, Map.of());
        ChunkWithText fetched = docstoreService.getChunk(stored.chunk().getId());

        assertThat(fetched.text()).isEqualTo("Retrieval text");
    }

    @Test
    void getChunk_throwsWhenMissing() {
        assertThatThrownBy(() -> docstoreService.getChunk(UUID.randomUUID()))
            .isInstanceOf(ChunkNotFoundException.class);
    }

    @Test
    void getChunksByDocument_returnsAllChunksInOrder() {
        Document doc = docstoreService.registerDocument("ext-007", "doc7.pdf", null);
        docstoreService.storeChunk(doc.getId(), 0, "chunk zero", 2, Map.of());
        docstoreService.storeChunk(doc.getId(), 1, "chunk one", 2, Map.of());
        docstoreService.storeChunk(doc.getId(), 2, "chunk two", 2, Map.of());

        List<ChunkWithText> chunks = docstoreService.getChunksByDocument(doc.getId());

        assertThat(chunks).hasSize(3);
        assertThat(chunks.get(0).text()).isEqualTo("chunk zero");
        assertThat(chunks.get(1).text()).isEqualTo("chunk one");
        assertThat(chunks.get(2).text()).isEqualTo("chunk two");
    }

    @Test
    void deleteChunksByDocument_removesAllChunksAndResetCount() {
        Document doc = docstoreService.registerDocument("ext-008", "doc8.pdf", null);
        docstoreService.storeChunk(doc.getId(), 0, "a", 1, Map.of());
        docstoreService.storeChunk(doc.getId(), 1, "b", 1, Map.of());

        int deleted = docstoreService.deleteChunksByDocument(doc.getId());

        assertThat(deleted).isEqualTo(2);
        assertThat(docstoreService.getChunksByDocument(doc.getId())).isEmpty();
        assertThat(docstoreService.getDocument(doc.getId()).getChunkCount()).isZero();
    }

    @Test
    void storeChunk_throwsWhenDocumentMissing() {
        assertThatThrownBy(() ->
            docstoreService.storeChunk(UUID.randomUUID(), 0, "text", 1, Map.of())
        ).isInstanceOf(DocumentNotFoundException.class);
    }
}
