package com.ewalletgateway.service.impl;

import com.ewalletgateway.api.dto.WalletFundRequest;
import com.ewalletgateway.api.dto.WalletOperationResponse;
import com.ewalletgateway.api.dto.WalletResponse;
import com.ewalletgateway.client.WalletRegistryPublicGrpcClient;
import com.ewalletgateway.service.interfaces.IWalletProxyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletProxyServiceImpl implements IWalletProxyService {

    private final WalletRegistryPublicGrpcClient walletRegistryPublicGrpcClient;

    @Override
    public WalletResponse getWallet(String authorizationHeader) {
        return walletRegistryPublicGrpcClient.getWallet(authorizationHeader);
    }

    @Override
    public WalletOperationResponse deposit(String authorizationHeader, WalletFundRequest body) {
        return walletRegistryPublicGrpcClient.deposit(authorizationHeader, body);
    }

    @Override
    public WalletOperationResponse withdraw(String authorizationHeader, WalletFundRequest body) {
        return walletRegistryPublicGrpcClient.withdraw(authorizationHeader, body);
    }
}
