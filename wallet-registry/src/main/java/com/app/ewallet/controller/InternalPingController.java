package com.app.ewallet.controller;

import com.app.ewallet.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal/v1")
@Tag(name = "Internal")
@SecurityRequirement(name = OpenApiConfig.INTERNAL_API_KEY_SCHEME)
public class InternalPingController {

    @GetMapping("/ping")
    @Operation(summary = "Kiểm tra internal API (cần X-Internal-Api-Key khi REQUIRE_INTERNAL_KEY=true)")
    public Map<String, String> ping() {
        return Map.of("status", "ok", "service", "wallet-registry");
    }
}
