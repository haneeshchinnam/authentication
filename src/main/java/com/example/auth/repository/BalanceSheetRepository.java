package com.example.auth.repository;

import com.example.auth.model.BalanceSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface BalanceSheetRepository extends JpaRepository<BalanceSheet, UUID> {

    @Query("SELECT b FROM BalanceSheet b WHERE b.transaction.id = :transactionId AND (b.payer.id = :userId OR b.payee.id = :userId)")
    Set<BalanceSheet> findByTransactionIdAndPayerIdOrPayeeId(@Param("transactionId") UUID transactionId, @Param("userId") Long userId);

    Set<BalanceSheet> findByTransactionId(UUID transactionId);
}
