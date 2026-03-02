package com.example.auth.interfaces;

import com.example.auth.dto.TransactionDto;
import org.springframework.http.ResponseEntity;

public interface SplitInterface {

    public ResponseEntity<?> createSplitTransaction(TransactionDto transactionDto);
    ResponseEntity<?> getSplitTransaction(String transactionId);
    ResponseEntity<?> getSplitTransactionsForUser(String userId);
    ResponseEntity<?> getSplitTransactionsForGroup(String groupId);
    ResponseEntity<?> settleSplitTransaction(String transactionId, String userId, double amount);
}
