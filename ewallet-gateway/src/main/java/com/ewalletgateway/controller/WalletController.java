package com.ewalletgateway.controller;

import com.ewalletgateway.api.dto.WalletFundRequest;
import com.ewalletgateway.api.dto.WalletOperationResponse;
import com.ewalletgateway.api.dto.WalletResponse;
import com.ewalletgateway.config.OpenApiConfig;
import com.ewalletgateway.security.AuthorizationHeaderAccessor;
import com.ewalletgateway.service.interfaces.IWalletProxyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallets")
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT_SCHEME)
public class WalletController {

    private final IWalletProxyService walletProxyService;

    @GetMapping
    @Operation(summary = "Lấy thông tin ví của user đăng nhập (mỗi user một ví)")
    public WalletResponse getWallet(Authentication authentication) {
        return walletProxyService.getWallet(AuthorizationHeaderAccessor.bearerHeader(authentication));
    }

    @PostMapping("/deposit")
    @Operation(summary = "Nạp tiền (idempotencyKey: UUID khuyến nghị, tối đa 128 ký tự)")
    public WalletOperationResponse deposit(
            @Valid @RequestBody WalletFundRequest body,
            Authentication authentication
    ) {
        return walletProxyService.deposit(AuthorizationHeaderAccessor.bearerHeader(authentication), body);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Rút tiền")
    public WalletOperationResponse withdraw(
            @Valid @RequestBody WalletFundRequest body,
            Authentication authentication
    ) {
        return walletProxyService.withdraw(AuthorizationHeaderAccessor.bearerHeader(authentication), body);
    }
}
