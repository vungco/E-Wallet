package com.app.ewallet.notificationworker.grpc;

import com.app.ewallet.notificationworker.service.NotificationNotFoundException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public final class NotificationGrpcExceptionMapper {

    private NotificationGrpcExceptionMapper() {
    }

    public static StatusRuntimeException toStatus(Throwable e) {
        Throwable t = e instanceof StatusRuntimeException sre ? sre : e;
        if (t instanceof StatusRuntimeException sre) {
            return sre;
        }
        if (t instanceof NotificationNotFoundException) {
            return Status.NOT_FOUND.withDescription(t.getMessage()).asRuntimeException();
        }
        if (t instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT.withDescription(t.getMessage()).asRuntimeException();
        }
        return Status.INTERNAL.withDescription(t.getMessage() != null ? t.getMessage() : "Internal error").asRuntimeException();
    }
}
