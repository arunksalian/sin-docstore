package ai.sinthanai.docstore.grpc;

import ai.sinthanai.docstore.domain.ChunkWithText;
import ai.sinthanai.docstore.domain.Document;
import ai.sinthanai.docstore.domain.DocumentStatus;
import ai.sinthanai.docstore.grpc.proto.*;
import ai.sinthanai.docstore.mapper.ProtoMapper;
import ai.sinthanai.docstore.service.DocstoreService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class DocstoreGrpcService extends DocstoreServiceGrpc.DocstoreServiceImplBase {

    private final DocstoreService docstoreService;
    private final ProtoMapper mapper;

    // -------------------------------------------------------------------------
    // Document RPCs
    // -------------------------------------------------------------------------

    @Override
    public void registerDocument(
        RegisterDocumentRequest request,
        StreamObserver<RegisterDocumentResponse> responseObserver
    ) {
        Document doc = docstoreService.registerDocument(
            request.getExternalId(),
            request.getSource(),
            request.getContentType().isBlank() ? null : request.getContentType()
        );
        responseObserver.onNext(RegisterDocumentResponse.newBuilder()
            .setDocument(mapper.toProto(doc))
            .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getDocument(
        GetDocumentRequest request,
        StreamObserver<GetDocumentResponse> responseObserver
    ) {
        UUID id = mapper.parseUuid(request.getId(), "id");
        Document doc = docstoreService.getDocument(id);
        responseObserver.onNext(GetDocumentResponse.newBuilder()
            .setDocument(mapper.toProto(doc))
            .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getDocumentByExternalId(
        GetDocumentByExternalIdRequest request,
        StreamObserver<GetDocumentResponse> responseObserver
    ) {
        Document doc = docstoreService.getDocumentByExternalId(request.getExternalId());
        responseObserver.onNext(GetDocumentResponse.newBuilder()
            .setDocument(mapper.toProto(doc))
            .build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateDocumentStatus(
        UpdateDocumentStatusRequest request,
        StreamObserver<UpdateDocumentStatusResponse> responseObserver
    ) {
        UUID id = mapper.parseUuid(request.getId(), "id");
        DocumentStatus status = mapper.fromProto(request.getStatus());
        String errorMessage = request.getErrorMessage().isBlank() ? null : request.getErrorMessage();
        Document doc = docstoreService.updateDocumentStatus(id, status, errorMessage);
        responseObserver.onNext(UpdateDocumentStatusResponse.newBuilder()
            .setDocument(mapper.toProto(doc))
            .build());
        responseObserver.onCompleted();
    }

    // -------------------------------------------------------------------------
    // Chunk RPCs
    // -------------------------------------------------------------------------

    @Override
    public void storeChunk(
        StoreChunkRequest request,
        StreamObserver<StoreChunkResponse> responseObserver
    ) {
        UUID documentId = mapper.parseUuid(request.getDocumentId(), "document_id");
        ChunkWithText cwt = docstoreService.storeChunk(
            documentId,
            request.getChunkIndex(),
            request.getText(),
            request.getTokenCount(),
            request.getMetadataMap()
        );
        responseObserver.onNext(StoreChunkResponse.newBuilder()
            .setChunk(mapper.toProto(cwt))
            .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getChunk(
        GetChunkRequest request,
        StreamObserver<GetChunkResponse> responseObserver
    ) {
        UUID id = mapper.parseUuid(request.getId(), "id");
        ChunkWithText cwt = docstoreService.getChunk(id);
        responseObserver.onNext(GetChunkResponse.newBuilder()
            .setChunk(mapper.toProto(cwt))
            .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getChunksByDocument(
        GetChunksByDocumentRequest request,
        StreamObserver<GetChunkResponse> responseObserver
    ) {
        UUID documentId = mapper.parseUuid(request.getDocumentId(), "document_id");
        List<ChunkWithText> chunks = docstoreService.getChunksByDocument(documentId);
        for (ChunkWithText cwt : chunks) {
            responseObserver.onNext(GetChunkResponse.newBuilder()
                .setChunk(mapper.toProto(cwt))
                .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void deleteChunksByDocument(
        DeleteChunksByDocumentRequest request,
        StreamObserver<DeleteChunksByDocumentResponse> responseObserver
    ) {
        UUID documentId = mapper.parseUuid(request.getDocumentId(), "document_id");
        int deleted = docstoreService.deleteChunksByDocument(documentId);
        responseObserver.onNext(DeleteChunksByDocumentResponse.newBuilder()
            .setDeletedCount(deleted)
            .build());
        responseObserver.onCompleted();
    }

    // -------------------------------------------------------------------------
    // Ops
    // -------------------------------------------------------------------------

    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        responseObserver.onNext(PingResponse.newBuilder().setStatus("ok").build());
        responseObserver.onCompleted();
    }
}
