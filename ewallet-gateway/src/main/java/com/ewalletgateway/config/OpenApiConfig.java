package com.ewalletgateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /** Dùng trong {@code @SecurityRequirement(name = ...)} trên controller / method */
    public static final String BEARER_JWT_SCHEME = "bearerJwt";

    @Bean
    OpenAPI ewalletGatewayOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ewallet-gateway")
                        .description("""
                                API REST công khai cho FE: đăng ký / đăng nhập, ví, chuyển tiền, tra trạng thái. \
                                Gateway gọi **wallet-registry** và **transfer-service** qua gRPC nội bộ.
                                """)
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_JWT_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Access token JWT (HS256), cùng secret/issuer với wallet-registry")));
    }
}
