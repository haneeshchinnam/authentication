package com.example.auth.repository;

import com.example.auth.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Set<Transaction> findByGroupId(UUID groupId);

    @Query("SELECT t FROM Transaction t WHERE t.paidBy.id = :userId OR t.borrowedBy.id = :userId")
    Set<Transaction> findByPaidByOrBorrowedBy(@Param("userId") Long userId);
}
