package com.app.ewallet.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String INTERNAL_API_KEY_SCHEME = "internalApiKey";
    public static final String BEARER_JWT_SCHEME = "bearerJwt";

    @Bean
    OpenAPI walletRegistryOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Wallet Registry API")
                        .description("""
                                **Auth:** `POST /api/v1/auth/register|login|refresh|logout` — công khai. `POST /api/v1/auth/logout/all` và **Wallets** cần header `Authorization: Bearer <access_token>`.

                                **Internal** (`/internal/**`): khi `REQUIRE_INTERNAL_KEY=true`, gửi `X-Internal-Api-Key` = `INTERNAL_API_KEY`.

                                **Refresh token:** lưu SHA-256 (64 hex) trong DB; client giữ plaintext refresh để gọi `/refresh`.
                                """)
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_JWT_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Access token JWT (HS256)"))
                        .addSecuritySchemes(INTERNAL_API_KEY_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Internal-Api-Key")));
    }
}
