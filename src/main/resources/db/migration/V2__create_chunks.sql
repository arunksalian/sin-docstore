CREATE TABLE chunks (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id  UUID        NOT NULL REFERENCES documents (id) ON DELETE CASCADE,
    chunk_index  INT         NOT NULL,
    blob_path    TEXT        NOT NULL,
    token_count  INT,
    metadata     JSONB,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_chunks_document_chunk UNIQUE (document_id, chunk_index)
);

CREATE INDEX idx_chunks_document_id ON chunks (document_id);
