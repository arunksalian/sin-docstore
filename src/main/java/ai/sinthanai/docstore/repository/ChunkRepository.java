package ai.sinthanai.docstore.repository;

import ai.sinthanai.docstore.domain.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChunkRepository extends JpaRepository<Chunk, UUID> {

    List<Chunk> findByDocumentIdOrderByChunkIndex(UUID documentId);

    @Modifying
    @Query("DELETE FROM Chunk c WHERE c.documentId = :documentId")
    int deleteByDocumentId(@Param("documentId") UUID documentId);
}
