package com.app.ewallet.service.interfaces;

import com.app.ewallet.controller.dto.InternalWalletOperationResponse;

import java.math.BigDecimal;

public interface IWalletLedgerService {

    InternalWalletOperationResponse debit(Long walletId, String idempotencyKey, BigDecimal amount);

    InternalWalletOperationResponse credit(Long walletId, String idempotencyKey, BigDecimal amount);
}
