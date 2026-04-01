package com.app.ewallet.service;

import com.app.ewallet.controller.dto.WalletResponse;

public interface IWalletQueryService {

    WalletResponse getWalletForUser(Long walletId, Long authenticatedUserId);
}
