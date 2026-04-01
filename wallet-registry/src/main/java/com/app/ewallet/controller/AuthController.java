package com.app.ewallet.controller;

import com.app.ewallet.config.OpenApiConfig;
import com.app.ewallet.controller.dto.LoginRequest;
import com.app.ewallet.controller.dto.RefreshTokenRequest;
import com.app.ewallet.controller.dto.RegisterRequest;
import com.app.ewallet.controller.dto.TokenResponse;
import com.app.ewallet.security.UserPrincipal;
import com.app.ewallet.service.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Auth")
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Đăng ký + tạo ví + cấp access & refresh token")
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập — cấp access & refresh token")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới access token (luân phiên refresh token)")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Thu hồi một refresh token")
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
    }

    @PostMapping("/logout/all")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT_SCHEME)
    @Operation(summary = "Thu hồi mọi refresh token của user (cần access token)")
    public void logoutAll(@AuthenticationPrincipal UserPrincipal principal) {
        authService.logoutAllSessions(principal.userId());
    }
}
