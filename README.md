# sin-docstore

Document and chunk metadata service for the **Sinthanai RAG platform**.

This is the foundation service. It owns one Postgres database (`sinthanai_docstore`) and one blob storage location. Every other Sinthanai service that needs chunk text reads it through this service's gRPC API — no other service touches the database or storage directly.

---

## Stack

| Concern | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Primary API | gRPC on port **9090** |
| Ops API | REST on port **8080** |
| Database | Postgres 16 (Flyway migrations) |
| Blob storage | Local filesystem (`local` profile) / S3-MinIO (`docker`, `prod`) |
| Build | Maven 3.9 (`./mvnw`) |

---

## Running locally

### Option A — Docker Compose (recommended)

Starts docstore + Postgres + MinIO in one command.

```bash
colima start          # start Docker runtime (macOS with Colima)
docker-compose up --build
```

| Endpoint | URL |
|---|---|
| gRPC | `localhost:9090` |
| REST / Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Health | `http://localhost:8080/actuator/health` |
| MinIO console | `http://localhost:9001` (minioadmin / minioadmin) |

### Option B — Maven + local Postgres

```bash
# Start a Postgres container (or use an existing instance)
docker run -d --name docstore-pg \
  -e POSTGRES_DB=sinthanai_docstore \
  -e POSTGRES_USER=docstore \
  -e POSTGRES_PASSWORD=docstore \
  -p 5432:5432 \
  postgres:16-alpine

./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Blob storage defaults to `/tmp/sin-docstore/blobs` in the `local` profile.

---

## Running tests

Testcontainers spins up Postgres automatically — no local database needed.

```bash
./mvnw test
```

---

## gRPC API

The full contract is in [`src/main/proto/docstore.proto`](src/main/proto/docstore.proto).

Install [`grpcurl`](https://github.com/fullstorydev/grpcurl) (`brew install grpcurl`) to call the service from the terminal.

### Register a document

```bash
grpcurl -plaintext -d '{
  "external_id": "doc-001",
  "source": "reports/q1-2026.pdf",
  "content_type": "application/pdf"
}' localhost:9090 sinthanai.docstore.v1.DocstoreService/RegisterDocument
```

### Store a chunk

```bash
grpcurl -plaintext -d '{
  "document_id": "<id>",
  "chunk_index": 0,
  "text": "Revenue grew 23% year-over-year...",
  "token_count": 12,
  "metadata": {"page": "1", "section": "Executive Summary"}
}' localhost:9090 sinthanai.docstore.v1.DocstoreService/StoreChunk
```

### Get a chunk

```bash
grpcurl -plaintext -d '{"id": "<chunk-id>"}' \
  localhost:9090 sinthanai.docstore.v1.DocstoreService/GetChunk
```

### Stream all chunks for a document

```bash
grpcurl -plaintext -d '{"document_id": "<doc-id>"}' \
  localhost:9090 sinthanai.docstore.v1.DocstoreService/GetChunksByDocument
```

### Update document status

```bash
grpcurl -plaintext -d '{
  "id": "<doc-id>",
  "status": "DOCUMENT_STATUS_INDEXED"
}' localhost:9090 sinthanai.docstore.v1.DocstoreService/UpdateDocumentStatus
```

### All available RPCs

| RPC | Description |
|---|---|
| `RegisterDocument` | Create a new document record |
| `GetDocument` | Fetch document by UUID |
| `GetDocumentByExternalId` | Fetch document by caller-assigned ID |
| `UpdateDocumentStatus` | Set status (`PENDING`, `PROCESSING`, `INDEXED`, `FAILED`) |
| `StoreChunk` | Write chunk text to blob storage and save metadata to DB |
| `GetChunk` | Fetch a single chunk (text + metadata) |
| `GetChunksByDocument` | Server-streaming: all chunks for a document in index order |
| `DeleteChunksByDocument` | Delete all chunks for a document from DB and blob storage |
| `Ping` | gRPC liveness check |

---

## REST / ops endpoints

| Endpoint | Description |
|---|---|
| `GET /api/v1/ping` | Liveness check |
| `GET /actuator/health` | Spring Boot health |
| `GET /actuator/metrics` | Micrometer metrics |
| `GET /actuator/prometheus` | Prometheus scrape endpoint |
| `GET /swagger-ui.html` | Swagger UI (ops endpoints only) |

---

## Configuration

| Property | Default | Description |
|---|---|---|
| `docstore.storage.local.root-path` | `/tmp/sin-docstore/blobs` | Blob root dir (`local` profile) |
| `docstore.storage.s3.endpoint` | — | S3 / MinIO endpoint URL |
| `docstore.storage.s3.bucket` | `sin-docstore` | Bucket name |
| `docstore.storage.s3.region` | `us-east-1` | AWS region |
| `docstore.storage.s3.access-key` | — | Access key |
| `docstore.storage.s3.secret-key` | — | Secret key |

Docker Compose environment variables (`DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, `S3_ENDPOINT`, `S3_BUCKET`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`) override all of the above.

---

## Architecture note

**One service = one database.** `sin-docstore` is the sole owner of `sinthanai_docstore` and its blob storage. All other Sinthanai services that need chunk text call this service over gRPC — they never connect to the database directly.
