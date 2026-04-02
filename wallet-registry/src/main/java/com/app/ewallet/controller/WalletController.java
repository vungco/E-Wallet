package com.app.ewallet.controller;

import com.app.ewallet.config.OpenApiConfig;
import com.app.ewallet.controller.dto.WalletResponse;
import com.app.ewallet.security.UserPrincipal;
import com.app.ewallet.service.interfaces.IWalletQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallets")
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT_SCHEME)
public class WalletController {

    private final IWalletQueryService walletQueryService;

    @GetMapping("/{walletId}")
    @Operation(summary = "Lấy thông tin ví (chỉ ví của user đăng nhập)")
    public WalletResponse getWallet(
            @PathVariable Long walletId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return walletQueryService.getWalletForUser(walletId, principal.userId());
    }
}
