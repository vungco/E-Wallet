package com.app.ewallet.grpc;

import com.app.ewallet.exception.ApiException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public final class TransferGrpcExceptionMapper {

    private TransferGrpcExceptionMapper() {
    }

    public static StatusRuntimeException toStatus(Throwable e) {
        Throwable t = unwrap(e);
        if (t instanceof StatusRuntimeException sre) {
            return sre;
        }
        if (t instanceof ApiException ae) {
            Status status = switch (ae.getStatus()) {
                case BAD_REQUEST -> Status.INVALID_ARGUMENT;
                case NOT_FOUND -> Status.NOT_FOUND;
                case CONFLICT -> Status.ALREADY_EXISTS;
                case FORBIDDEN -> Status.PERMISSION_DENIED;
                case UNAUTHORIZED -> Status.UNAUTHENTICATED;
                default -> Status.INTERNAL;
            };
            return status.withDescription(ae.getMessage()).asRuntimeException();
        }
        if (t instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT.withDescription(t.getMessage()).asRuntimeException();
        }
        if (t instanceof NumberFormatException) {
            return Status.INVALID_ARGUMENT.withDescription("Invalid amount: " + t.getMessage()).asRuntimeException();
        }
        return Status.INTERNAL.withDescription(t.getMessage() != null ? t.getMessage() : "Internal error").asRuntimeException();
    }

    private static Throwable unwrap(Throwable e) {
        Throwable c = e;
        int depth = 0;
        while (c.getCause() != null && depth++ < 10) {
            if (c instanceof ApiException || c instanceof StatusRuntimeException || c instanceof IllegalArgumentException) {
                break;
            }
            c = c.getCause();
        }
        return c;
    }
}
