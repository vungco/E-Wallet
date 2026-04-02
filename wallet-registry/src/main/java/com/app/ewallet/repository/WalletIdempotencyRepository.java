package com.app.ewallet.repository;

import com.app.ewallet.model.WalletIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletIdempotencyRepository extends JpaRepository<WalletIdempotency, Long> {

    Optional<WalletIdempotency> findByIdempotencyKey(String idempotencyKey);
}
