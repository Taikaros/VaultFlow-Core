package com.vaultflow.repository;

import com.vaultflow.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByToWalletId(String toWalletId);
    List<Transaction> findByFromCardId(String fromCardId);
    List<Transaction> findByStatus(String status);
    List<Transaction> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
