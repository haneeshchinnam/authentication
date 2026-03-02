package com.example.auth.service;

import com.example.auth.dto.SplitType;
import com.example.auth.dto.TransactionDto;
import com.example.auth.interfaces.SplitInterface;
import com.example.auth.model.BalanceSheet;
import com.example.auth.model.Group;
import com.example.auth.model.Transaction;
import com.example.auth.model.User;
import com.example.auth.repository.BalanceSheetRepository;
import com.example.auth.repository.GroupRepository;
import com.example.auth.repository.TransactionRepository;
import com.example.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class SplitService implements SplitInterface {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BalanceSheetRepository balanceSheetRepository;

    @Override
    public ResponseEntity<?> createSplitTransaction(TransactionDto transactionDto) {
        // Create and save the transaction
        Transaction transaction = saveTransaction(transactionDto);

        // Fetch necessary entities
        User paidByUser = userRepository.findByUsername(transactionDto.getPaidBy())
                .orElseThrow(() -> new RuntimeException("User not found: " + transactionDto.getPaidBy()));
        Optional<User> borrowedByUser = userRepository.findByUsername(transactionDto.getBorrowedBy());
        Optional<Group> group = groupRepository.findById(UUID.fromString(transactionDto.getGroupId()));

        // Validate group and split values
        validateGroupAndSplitValues(transactionDto, borrowedByUser, group);

        // Create BalanceSheet entries
        if (borrowedByUser.isPresent()) {
            createBalanceSheetEntriesForBorrowed(transactionDto, transaction, paidByUser, borrowedByUser.get());
        } else if (group.isPresent() && !group.get().getUsers().isEmpty()) {
            createBalanceSheetEntriesForGroup(transactionDto, transaction, paidByUser, group.get());
        } else {
            throw new RuntimeException("Groups size is zero, cannot split transaction");
        }

        return ResponseEntity.ok("Split transactions created successfully!");
    }

    @Override
    public ResponseEntity<?> getSplitTransaction(String transactionId) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(UUID.fromString(transactionId));
        if (transactionOpt.isPresent()) {
            Transaction transaction = transactionOpt.get();
            return ResponseEntity.ok(transaction);
        }
        return ResponseEntity.badRequest().body("Transaction not found");
    }

    @Override
    public ResponseEntity<?> getSplitTransactionsForUser(String userId) {
        Optional<User> userOpt = userRepository.findById(Long.getLong(userId));
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(transactionRepository.findByPaidByOrBorrowedBy(Long.getLong(userId)));
        }
        return ResponseEntity.badRequest().body("User not found");
    }

    @Override
    public ResponseEntity<?> getSplitTransactionsForGroup(String groupId) {
        // Validate if the group exists
        Optional<Group> groupOpt = groupRepository.findById(UUID.fromString(groupId));
        if (groupOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Group not found with ID: " + groupId);
        }

        Group group = groupOpt.get();

        // Fetch transactions for the group
        Set<Transaction> transactions = transactionRepository.findByGroupId(group.getId());
        if (transactions.isEmpty()) {
            return ResponseEntity.ok("No transactions found for the group");
        }

        return ResponseEntity.ok(transactions);
    }

    @Override
    public ResponseEntity<?> settleSplitTransaction(String transactionId, String userId, double amount) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(UUID.fromString(transactionId));
        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));

        if (transactionOpt.isPresent() && userOpt.isPresent()) {
            Transaction transaction = transactionOpt.get();
            User borrower = userOpt.get();

            // Check if user is involved, and in this case, the user must be a borrower
            // paying back the paidBy user
            if ((transaction.getBorrowedBy() != null
                    && Objects.equals(transaction.getBorrowedBy().getId(), borrower.getId())) ||
                    (transaction.getGroup() != null && transaction.getGroup().getUsers().contains(borrower))) {

                // User is paying back the transaction.paidBy()
                User payer = transaction.getPaidBy();

                // Create a balance sheet record for this payment
                BalanceSheet paymentRecord = new BalanceSheet();
                paymentRecord.setTransaction(transaction);
                paymentRecord.setPayer(borrower); // the person paying the money back
                paymentRecord.setPayee(payer); // the person who originally paid for the transaction
                paymentRecord.setAmount(amount); // positive amount indicating payment
                paymentRecord.setCreatedAt(OffsetDateTime.now().toString());
                balanceSheetRepository.save(paymentRecord);

                // Check if all borrowers have paid the full amount
                Set<BalanceSheet> allBalances = balanceSheetRepository.findByTransactionId(transaction.getId());

                double totalPaidBack = 0;
                for (BalanceSheet bs : allBalances) {
                    // We only sum up the positive payments made towards the original payer
                    if (Objects.equals(bs.getPayee().getId(), payer.getId()) && bs.getAmount() > 0) {
                        totalPaidBack += bs.getAmount();
                    }
                }

                if (totalPaidBack >= transaction.getAmount()) {
                    transaction.setIsSettled(true);
                    transactionRepository.save(transaction);
                }

                return ResponseEntity.ok("Payment recorded successfully"
                        + (transaction.getIsSettled() ? " and transaction settled" : ""));
            } else {
                return ResponseEntity.badRequest().body("User is not involved in this transaction as a borrower");
            }
        }
        return ResponseEntity.badRequest().body("Transaction or User not found");
    }

    private Transaction saveTransaction(TransactionDto transactionDto) {
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionDto.getAmount());
        transaction.setDescription(transactionDto.getDescription());
        transaction.setCreatedAt(OffsetDateTime.now().toString());
        transaction.setPaidBy(userRepository.findByUsername(transactionDto.getPaidBy())
                .orElseThrow(() -> new RuntimeException("User not found: " + transactionDto.getPaidBy())));

        // Check if borrowedBy is not null or empty
        if (transactionDto.getBorrowedBy() != null && !transactionDto.getBorrowedBy().isEmpty()) {
            transaction.setBorrowedBy(userRepository.findByUsername(transactionDto.getBorrowedBy()).orElse(null));
        }

        // Check if groupId is not null or empty
        if (transactionDto.getGroupId() != null && !transactionDto.getGroupId().isEmpty()) {
            transaction.setGroup(groupRepository.findById(UUID.fromString(transactionDto.getGroupId())).orElse(null));
        }

        return transactionRepository.saveAndFlush(transaction);
    }

    private void validateGroupAndSplitValues(TransactionDto transactionDto, Optional<User> borrowedByUser,
            Optional<Group> group) {
        if (borrowedByUser.isEmpty() && transactionDto.getSplitType() != SplitType.EQUAL) {
            if (group.isEmpty() || group.get().getUsers().size() != transactionDto.getSplitValues().size()) {
                throw new RuntimeException("Split values size must match the number of users in the group");
            }
        }
    }

    private void createBalanceSheetEntriesForBorrowed(TransactionDto transactionDto, Transaction transaction,
            User paidByUser, User borrowedByUser) {
        double halfAmount = transactionDto.getAmount() / 2;
        balanceSheetRepository.save(createBalanceSheet(transaction, paidByUser, borrowedByUser, halfAmount));
        balanceSheetRepository.save(createBalanceSheet(transaction, borrowedByUser, paidByUser, -halfAmount));
    }

    private void createBalanceSheetEntriesForGroup(TransactionDto transactionDto, Transaction transaction,
            User paidByUser, Group group) {
        int userCount = group.getUsers().size();
        if (SplitType.EQUAL.equals(transactionDto.getSplitType())) {
            double equalShare = transactionDto.getAmount() / userCount;
            group.getUsers().stream()
                    .filter(user -> !user.getUsername().equals(transactionDto.getPaidBy()))
                    .forEach(user -> balanceSheetRepository
                            .save(createBalanceSheet(transaction, paidByUser, user, -equalShare)));
        } else {
            int i = 0;
            for (User user : group.getUsers()) {
                if (!user.getUsername().equals(transactionDto.getPaidBy())) {
                    double amount = (transactionDto.getSplitType() == SplitType.PERCENTAGE)
                            ? transactionDto.getAmount() * transactionDto.getSplitValues().get(i) / 100
                            : transactionDto.getSplitValues().get(i);
                    balanceSheetRepository.save(createBalanceSheet(transaction, paidByUser, user, -amount));
                    i++;
                }
            }
        }
    }

    private BalanceSheet createBalanceSheet(Transaction transaction, User payer, User payee, double amount) {
        BalanceSheet balanceSheet = new BalanceSheet();
        balanceSheet.setTransaction(transaction);
        balanceSheet.setPayer(payer);
        balanceSheet.setPayee(payee);
        balanceSheet.setAmount(amount);
        balanceSheet.setCreatedAt(OffsetDateTime.now().toString());
        return balanceSheet;
    }

}
