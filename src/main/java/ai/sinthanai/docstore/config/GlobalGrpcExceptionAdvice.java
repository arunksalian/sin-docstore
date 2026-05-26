package ai.sinthanai.docstore.config;

import ai.sinthanai.docstore.exception.BlobStorageException;
import ai.sinthanai.docstore.exception.ChunkNotFoundException;
import ai.sinthanai.docstore.exception.DocumentNotFoundException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GlobalGrpcExceptionAdvice {

    @GrpcExceptionHandler(DocumentNotFoundException.class)
    public StatusRuntimeException handleDocumentNotFound(DocumentNotFoundException e) {
        return Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(ChunkNotFoundException.class)
    public StatusRuntimeException handleChunkNotFound(ChunkNotFoundException e) {
        return Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(BlobStorageException.class)
    public StatusRuntimeException handleBlobStorage(BlobStorageException e) {
        return Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusRuntimeException handleIllegalArgument(IllegalArgumentException e) {
        return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(Exception.class)
    public StatusRuntimeException handleGeneral(Exception e) {
        return Status.INTERNAL.withDescription("Internal server error: " + e.getMessage()).asRuntimeException();
    }
}
