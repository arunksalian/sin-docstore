package ai.sinthanai.docstore.repository;

import ai.sinthanai.docstore.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Optional<Document> findByExternalId(String externalId);

    @Modifying
    @Query("UPDATE Document d SET d.chunkCount = d.chunkCount + 1 WHERE d.id = :id")
    void incrementChunkCount(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Document d SET d.chunkCount = 0 WHERE d.id = :id")
    void resetChunkCount(@Param("id") UUID id);
}
