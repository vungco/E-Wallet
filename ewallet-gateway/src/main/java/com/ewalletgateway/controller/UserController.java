package com.ewalletgateway.controller;

import com.ewalletgateway.api.dto.UserLookupResponse;
import com.ewalletgateway.config.OpenApiConfig;
import com.ewalletgateway.security.AuthorizationHeaderAccessor;
import com.ewalletgateway.service.interfaces.IUserLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users")
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT_SCHEME)
public class UserController {

    private final IUserLookupService userLookupService;

    @GetMapping("/lookup")
    @Operation(summary = "Tra user + ví theo email (dùng trước khi chuyển tiền; không trả số dư)")
    public UserLookupResponse lookupByEmail(
            @RequestParam("email") String email,
            Authentication authentication
    ) {
        return userLookupService.lookupByEmail(AuthorizationHeaderAccessor.bearerHeader(authentication), email);
    }
}
