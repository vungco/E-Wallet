package com.app.ewallet.service.impl;

import com.app.ewallet.controller.dto.UserLookupResponse;
import com.app.ewallet.controller.dto.WalletResponse;
import com.app.ewallet.exception.UserNotFoundByEmailException;
import com.app.ewallet.exception.WalletNotFoundException;
import com.app.ewallet.model.User;
import com.app.ewallet.model.Wallet;
import com.app.ewallet.repository.UserRepository;
import com.app.ewallet.repository.WalletRepository;
import com.app.ewallet.service.interfaces.IWalletQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class WalletQueryServiceImpl implements IWalletQueryService {

    private final UserRepository userRepository;
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

    @Override
    @Transactional(readOnly = true)
    public UserLookupResponse lookupUserByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("email must not be blank");
        }
        String trimmed = email.trim();
        User user = userRepository.findByEmail(trimmed)
                .orElseThrow(() -> new UserNotFoundByEmailException(trimmed));
        Wallet wallet = walletRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + user.getId()));
        return new UserLookupResponse(
                user.getId(),
                wallet.getId(),
                user.getEmail(),
                user.getName()
        );
    }
}
