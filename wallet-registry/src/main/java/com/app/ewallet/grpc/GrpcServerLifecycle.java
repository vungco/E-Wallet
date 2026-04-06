package com.app.ewallet.grpc;

import com.app.ewallet.config.properties.GrpcServerProperties;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.protobuf.services.ProtoReflectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * gRPC server Netty (cổng tách khỏi HTTP Spring).
 */
@Component
@ConditionalOnProperty(name = "app.grpc.server.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class GrpcServerLifecycle implements SmartLifecycle {

    private final GrpcServerProperties grpcServerProperties;
    private final WalletRegistryPublicGrpcService walletRegistryPublicGrpcService;
    private final GatewayRegistryGrpcInterceptor gatewayRegistryGrpcInterceptor;

    private Server server;
    private volatile boolean running;

    @Override
    public void start() {
        if (running || !grpcServerProperties.enabled()) {
            return;
        }
        try {
            ServerBuilder<?> builder = ServerBuilder.forPort(grpcServerProperties.port())
                    .maxInboundMessageSize(grpcServerProperties.maxInboundMessageSizeBytes())
                    .addService(ServerInterceptors.intercept(walletRegistryPublicGrpcService, gatewayRegistryGrpcInterceptor));
            if (grpcServerProperties.reflectionEnabled()) {
                builder.addService(ProtoReflectionService.newInstance());
                log.info("gRPC server reflection enabled (disable in production if unused)");
            }
            server = builder.build().start();
            running = true;
            log.info("gRPC server listening on port {}", grpcServerProperties.port());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot start gRPC server on port " + grpcServerProperties.port(), e);
        }
    }

    @Override
    public void stop() {
        if (!running || server == null) {
            return;
        }
        server.shutdown();
        try {
            if (!server.awaitTermination(10, TimeUnit.SECONDS)) {
                server.shutdownNow();
            }
        } catch (InterruptedException e) {
            server.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            running = false;
            server = null;
        }
        log.info("gRPC server stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
