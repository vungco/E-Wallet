package com.ewalletgateway.controller;

import com.ewalletgateway.api.dto.LoginRequest;
import com.ewalletgateway.api.dto.RefreshTokenRequest;
import com.ewalletgateway.api.dto.RegisterRequest;
import com.ewalletgateway.api.dto.RegisterResponse;
import com.ewalletgateway.api.dto.TokenResponse;
import com.ewalletgateway.client.WalletRegistryPublicGrpcClient;
import com.ewalletgateway.config.OpenApiConfig;
import com.ewalletgateway.security.AuthorizationHeaderAccessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final WalletRegistryPublicGrpcClient walletRegistryPublicGrpcClient;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Đăng ký + tạo ví (không cấp token — dùng login sau)")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return walletRegistryPublicGrpcClient.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập — cấp access + refresh token")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return walletRegistryPublicGrpcClient.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới access token")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return walletRegistryPublicGrpcClient.refresh(request);
    }

    @PostMapping("/logout")
    @Operation(summary = "Thu hồi một refresh token (cần access token — đã đăng nhập)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT_SCHEME)
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        walletRegistryPublicGrpcClient.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout/all")
    @Operation(summary = "Thu hồi mọi refresh token của user (Bearer từ nút Authorize — không nhập header thêm)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT_SCHEME)
    public ResponseEntity<Void> logoutAll(Authentication authentication) {
        walletRegistryPublicGrpcClient.logoutAll(AuthorizationHeaderAccessor.bearerHeader(authentication));
        return ResponseEntity.ok().build();
    }
}
