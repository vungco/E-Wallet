package com.app.ewallet.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.grpc.server")
public record GrpcServerProperties(
        boolean enabled,
        int port,
        /**
         * Server reflection: Postman / grpcurl list service không cần file .proto.
         * Nên tắt trên production ({@code GRPC_REFLECTION_ENABLED=false}).
         */
        boolean reflectionEnabled,
        /** Giới hạn kích thước message inbound (bytes). */
        int maxInboundMessageSizeBytes
) {
}
