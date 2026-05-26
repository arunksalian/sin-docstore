CREATE TABLE documents (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    external_id  VARCHAR(512) NOT NULL,
    source       TEXT         NOT NULL,
    content_type VARCHAR(255),
    status       VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    chunk_count  INT          NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_documents_external_id ON documents (external_id);
CREATE INDEX        idx_documents_status      ON documents (status);
