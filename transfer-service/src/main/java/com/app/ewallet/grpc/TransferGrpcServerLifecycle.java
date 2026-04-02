package com.app.ewallet.grpc;

import com.app.ewallet.config.properties.GrpcServerProperties;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "app.grpc.server.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class TransferGrpcServerLifecycle implements SmartLifecycle {

    private final GrpcServerProperties grpcServerProperties;
    private final TransferGrpcEndpoint transferGrpcEndpoint;
    private final JwtGrpcInterceptor jwtGrpcInterceptor;

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
                    .addService(ServerInterceptors.intercept(transferGrpcEndpoint, jwtGrpcInterceptor));
            if (grpcServerProperties.reflectionEnabled()) {
                builder.addService(loadProtoReflectionService());
                log.info("gRPC transfer API reflection enabled (disable in production if unused)");
            }
            server = builder.build().start();
            running = true;
            log.info("Transfer gRPC server listening on port {}", grpcServerProperties.port());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot start transfer gRPC server on port " + grpcServerProperties.port(), e);
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
        log.info("Transfer gRPC server stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    /**
     * Không import tĩnh {@code ProtoReflectionService} — tránh lỗi classloader (vd. DevTools restart)
     * khi JAR grpc-services chưa được nạp đúng.
     */
    private static BindableService loadProtoReflectionService() {
        try {
            Class<?> clazz = Class.forName("io.grpc.protobuf.services.ProtoReflectionService");
            return (BindableService) clazz.getMethod("newInstance").invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Add dependency io.grpc:grpc-services and ensure it is on the classpath (see META-INF/spring-devtools.properties if using DevTools)",
                    e
            );
        }
    }
}
