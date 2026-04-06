package com.app.ewallet.grpc;

import io.grpc.Context;
import io.grpc.Metadata;

/**
 * Metadata gRPC từng request (đặt bởi {@link GatewayRegistryGrpcInterceptor}).
 */
public final class RegistryGrpcContext {

    public static final Context.Key<Metadata> HEADERS = Context.key("registry-headers");

    private RegistryGrpcContext() {
    }
}
