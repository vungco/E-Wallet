package com.ewalletgateway.web;

import com.ewalletgateway.api.dto.AcceptedTransferResponse;
import com.ewalletgateway.api.dto.CreateTransferRequest;
import com.ewalletgateway.api.dto.TransferStatusResponse;
import com.ewalletgateway.service.TransferGrpcBackend;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferGrpcBackend transferGrpcBackend;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AcceptedTransferResponse create(
            @Valid @RequestBody CreateTransferRequest body,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        requireBearer(authorization);
        return transferGrpcBackend.createTransfer(authorization, body);
    }

    @GetMapping("/{requestId}")
    public TransferStatusResponse get(
            @PathVariable String requestId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        requireBearer(authorization);
        return transferGrpcBackend.getTransferStatus(authorization, requestId);
    }

    private static void requireBearer(String authorization) {
        if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing or invalid Authorization header");
        }
    }
}
