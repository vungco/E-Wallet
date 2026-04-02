package com.app.ewallet.service.impl;

import com.app.ewallet.controller.dto.InternalWalletOperationResponse;
import com.app.ewallet.exception.IdempotencyConflictException;
import com.app.ewallet.exception.InsufficientBalanceException;
import com.app.ewallet.exception.WalletNotFoundException;
import com.app.ewallet.model.Wallet;
import com.app.ewallet.model.WalletIdempotency;
import com.app.ewallet.model.WalletOperation;
import com.app.ewallet.repository.WalletIdempotencyRepository;
import com.app.ewallet.repository.WalletRepository;
import com.app.ewallet.service.interfaces.IWalletLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class WalletLedgerServiceImpl implements IWalletLedgerService {

    private static final int MONEY_SCALE = 4;

    private final WalletRepository walletRepository;
    private final WalletIdempotencyRepository walletIdempotencyRepository;

    @Override
    @Transactional
    public InternalWalletOperationResponse debit(Long walletId, String idempotencyKey, BigDecimal amount) {
        BigDecimal normalized = normalizeAmount(amount);
        Wallet wallet = walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        WalletIdempotency existing = walletIdempotencyRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existing != null) {
            assertIdempotentMatch(existing, walletId, WalletOperation.DEBIT, normalized);
            return new InternalWalletOperationResponse(
                    wallet.getId(),
                    existing.getBalanceAfter(),
                    existing.getWalletVersionSnapshot(),
                    true
            );
        }

        if (wallet.getBalance().compareTo(normalized) < 0) {
            throw new InsufficientBalanceException();
        }
        wallet.setBalance(wallet.getBalance().subtract(normalized).setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        walletRepository.save(wallet);

        persistIdempotency(idempotencyKey, wallet, WalletOperation.DEBIT, normalized);
        return new InternalWalletOperationResponse(wallet.getId(), wallet.getBalance(), wallet.getVersion(), false);
    }

    @Override
    @Transactional
    public InternalWalletOperationResponse credit(Long walletId, String idempotencyKey, BigDecimal amount) {
        BigDecimal normalized = normalizeAmount(amount);
        Wallet wallet = walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        WalletIdempotency existing = walletIdempotencyRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existing != null) {
            assertIdempotentMatch(existing, walletId, WalletOperation.CREDIT, normalized);
            return new InternalWalletOperationResponse(
                    wallet.getId(),
                    existing.getBalanceAfter(),
                    existing.getWalletVersionSnapshot(),
                    true
            );
        }

        wallet.setBalance(wallet.getBalance().add(normalized).setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        walletRepository.save(wallet);

        persistIdempotency(idempotencyKey, wallet, WalletOperation.CREDIT, normalized);
        return new InternalWalletOperationResponse(wallet.getId(), wallet.getBalance(), wallet.getVersion(), false);
    }

    private void assertIdempotentMatch(
            WalletIdempotency existing,
            Long walletId,
            WalletOperation operation,
            BigDecimal amount
    ) {
        if (!existing.getWallet().getId().equals(walletId)
                || existing.getOperation() != operation
                || existing.getAmount().compareTo(amount) != 0) {
            throw new IdempotencyConflictException();
        }
    }

    private void persistIdempotency(
            String idempotencyKey,
            Wallet wallet,
            WalletOperation operation,
            BigDecimal amount
    ) {
        WalletIdempotency row = new WalletIdempotency();
        row.setIdempotencyKey(idempotencyKey);
        row.setWallet(wallet);
        row.setOperation(operation);
        row.setAmount(amount);
        row.setBalanceAfter(wallet.getBalance());
        row.setWalletVersionSnapshot(wallet.getVersion());
        walletIdempotencyRepository.save(row);
    }

    private static BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("amount is required");
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
