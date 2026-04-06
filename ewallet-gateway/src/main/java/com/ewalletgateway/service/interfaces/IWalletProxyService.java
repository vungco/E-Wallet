package com.ewalletgateway.service.interfaces;

import com.ewalletgateway.api.dto.WalletFundRequest;
import com.ewalletgateway.api.dto.WalletOperationResponse;
import com.ewalletgateway.api.dto.WalletResponse;

public interface IWalletProxyService {

    WalletResponse getWallet(String authorizationHeader);

    WalletOperationResponse deposit(String authorizationHeader, WalletFundRequest body);

    WalletOperationResponse withdraw(String authorizationHeader, WalletFundRequest body);
}
