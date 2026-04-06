package com.app.ewallet.grpc;

import io.grpc.Metadata;

/**
 * Metadata gRPC tương đương header REST internal.
 */
public final class GrpcMetadataKeys {

    public static final Metadata.Key<String> X_INTERNAL_API_KEY =
            Metadata.Key.of("x-internal-api-key", Metadata.ASCII_STRING_MARSHALLER);

    public static final Metadata.Key<String> IDEMPOTENCY_KEY =
            Metadata.Key.of("idempotency-key", Metadata.ASCII_STRING_MARSHALLER);

    /** Client gửi: {@code Bearer <access_token>} */
    public static final Metadata.Key<String> AUTHORIZATION =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    private GrpcMetadataKeys() {
    }
}
