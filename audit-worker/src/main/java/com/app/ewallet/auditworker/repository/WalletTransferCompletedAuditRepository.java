package com.app.ewallet.auditworker.repository;

import com.app.ewallet.auditworker.model.WalletTransferCompletedAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransferCompletedAuditRepository extends JpaRepository<WalletTransferCompletedAudit, Long> {

    boolean existsByTransactionId(long transactionId);
}
