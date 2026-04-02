package com.app.ewallet.repository;

import com.app.ewallet.model.Transfer;
import com.app.ewallet.model.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    Optional<Transfer> findByRequestId(String requestId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Transfer t SET t.status = :newStatus WHERE t.requestId = :rid AND t.status = :expected")
    int updateStatusIf(
            @Param("rid") String requestId,
            @Param("expected") TransferStatus expected,
            @Param("newStatus") TransferStatus newStatus
    );
}
