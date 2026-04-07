package com.ewalletgateway.grpc;

import io.grpc.Metadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Hằng metadata gRPC dùng chữ gateway và resolve {@code x-internal-api-key}
 * (YAML theo service rồi fallback {@code INTERNAL_API_KEY}).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayGrpcMetadataSupport {

    public static final Metadata.Key<String> AUTHORIZATION =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> X_INTERNAL_API_KEY =
            Metadata.Key.of("x-internal-api-key", Metadata.ASCII_STRING_MARSHALLER);

    private final Environment environment;

    /**
     * Giá trị từ {@code app.*.internal-api-key}; nếu trống thì {@link Environment#getProperty(String)} {@code INTERNAL_API_KEY}.
     */
    public String resolveInternalApiKey(String configuredPrimary) {
        String raw = configuredPrimary;
        if (!StringUtils.hasText(raw)) {
            raw = environment.getProperty("INTERNAL_API_KEY");
        }
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Gắn {@code x-internal-api-key} khi resolve được; nếu không thì log WARN (context để phân biệt registry vs transfer).
     */
    public void putInternalApiKey(Metadata md, String configuredPrimary, String logContext) {
        String key = resolveInternalApiKey(configuredPrimary);
        if (key != null) {
            md.put(X_INTERNAL_API_KEY, key);
        } else {
            log.warn(
                    "[{}] gRPC: no internal API key — set service internal-api-key in YAML or INTERNAL_API_KEY",
                    logContext
            );
        }
    }
}
