package com.ewalletgateway.controller;

import com.ewalletgateway.api.dto.TransferStatusResponse;
import com.ewalletgateway.config.OpenApiConfig;
import com.ewalletgateway.security.AuthorizationHeaderAccessor;
import com.ewalletgateway.service.interfaces.ITransferGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions")
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT_SCHEME)
public class TransactionController {

    private final ITransferGatewayService transferGatewayService;

    @GetMapping("/{requestId}")
    @Operation(summary = "Tra trạng thái giao dịch theo requestId (Bearer từ Authorize — không nhập header thêm)")
    public TransferStatusResponse getByRequestId(
            @PathVariable String requestId,
            Authentication authentication
    ) {
        return transferGatewayService.getTransferStatus(
                AuthorizationHeaderAccessor.bearerHeader(authentication),
                requestId
        );
    }
}
