package com.ewalletgateway.controller;

import com.ewalletgateway.api.dto.AcceptedTransferResponse;
import com.ewalletgateway.api.dto.CreateTransferRequest;
import com.ewalletgateway.config.OpenApiConfig;
import com.ewalletgateway.security.AuthorizationHeaderAccessor;
import com.ewalletgateway.service.interfaces.ITransferGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers")
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT_SCHEME)
public class TransferController {

    private final ITransferGatewayService transferGatewayService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Tạo lệnh chuyển tiền (Bearer từ Authorize — 202 + requestId; xử lý async)")
    public AcceptedTransferResponse create(
            @Valid @RequestBody CreateTransferRequest body,
            Authentication authentication
    ) {
        return transferGatewayService.createTransfer(
                AuthorizationHeaderAccessor.bearerHeader(authentication),
                body
        );
    }
}
