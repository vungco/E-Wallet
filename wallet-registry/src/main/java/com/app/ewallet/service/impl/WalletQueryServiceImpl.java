package com.app.ewallet.service.impl;

import com.app.ewallet.controller.dto.WalletResponse;
import com.app.ewallet.exception.WalletNotFoundException;
import com.app.ewallet.model.Wallet;
import com.app.ewallet.repository.WalletRepository;
import com.app.ewallet.service.interfaces.IWalletQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletQueryServiceImpl implements IWalletQueryService {

    private final WalletRepository walletRepository;

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletByUserId(Long userId) {
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));
        return new WalletResponse(
                wallet.getId(),
                wallet.getUser().getId(),
                wallet.getUser().getName(),
                wallet.getBalance(),
                wallet.getVersion()
        );
    }
}
