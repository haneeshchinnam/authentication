package com.example.auth.controller;

import com.example.auth.dto.TransactionDto;
import com.example.auth.interfaces.SplitInterface;
import com.example.auth.validator.TransactionValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SplitController {

    private final TransactionValidator transactionValidator;

    private final SplitInterface splitInterface;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(transactionValidator);
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getAllSplitTransactionsForUserId(
            @RequestParam("user_id") String userId
    ) {
        // Simulate fetching all split transactions
        return ResponseEntity.ok("List of all split transactions");
    }

    @GetMapping("/transaction/total")
    public ResponseEntity<?> getTotalDebtBasedOnOneTransaction(
            @RequestParam("transaction_id") String transactionId,
            @RequestParam("user_id") String userId
    ) {
        // Simulate a total based on one transaction
        double total = 100.00; // This would be calculated based on the transaction details
        return ResponseEntity.ok("Total based on one transaction: $" + total);
    }

    @PostMapping("/split")
    public ResponseEntity<?> createSplitTransaction(@Valid @RequestBody TransactionDto transactionDto) {
        // Simulate creating split transactions
        // In a real application, you would process the transaction details and create multiple entries in the database
        return splitInterface.createSplitTransaction(transactionDto);
    }

    @PostMapping("/settle")
    public ResponseEntity<?> settleTransaction(
            @RequestParam("transaction_id") String transactionId,
            @RequestParam("user_id") String userId,
            @RequestParam("amount") Double amount
    ) {
        // Simulate settling a transaction
        return splitInterface.settleSplitTransaction(transactionId, userId, amount);
    }
}
