package com.app.ewallet.notificationworker.grpc;

import com.app.ewallet.notificationworker.config.properties.GrpcServerProperties;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationGrpcServerLifecycle implements SmartLifecycle {

    private final GrpcServerProperties grpcServerProperties;
    private final NotificationGrpcEndpoint notificationGrpcEndpoint;
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
                    .addService(ServerInterceptors.intercept(notificationGrpcEndpoint, jwtGrpcInterceptor));
            if (grpcServerProperties.reflectionEnabled()) {
                builder.addService(loadProtoReflectionService());
                log.info("gRPC notification API reflection enabled (disable in production if unused)");
            }
            server = builder.build().start();
            running = true;
            log.info("Notification gRPC server listening on port {}", grpcServerProperties.port());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot start notification gRPC server on port " + grpcServerProperties.port(), e);
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
        log.info("Notification gRPC server stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    private static BindableService loadProtoReflectionService() {
        try {
            Class<?> clazz = Class.forName("io.grpc.protobuf.services.ProtoReflectionService");
            return (BindableService) clazz.getMethod("newInstance").invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Add dependency io.grpc:grpc-services", e);
        }
    }
}
