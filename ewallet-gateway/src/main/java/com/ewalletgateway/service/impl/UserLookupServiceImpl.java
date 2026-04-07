package com.ewalletgateway.service.impl;

import com.ewalletgateway.api.dto.UserLookupResponse;
import com.ewalletgateway.client.WalletRegistryPublicGrpcClient;
import com.ewalletgateway.service.interfaces.IUserLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserLookupServiceImpl implements IUserLookupService {

    private final WalletRegistryPublicGrpcClient grpcClient;

    @Override
    public UserLookupResponse lookupByEmail(String authorizationHeader, String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("email must not be blank");
        }
        return grpcClient.lookupUserByEmail(authorizationHeader, email.trim());
    }
}
