package com.vaultflow.repository;

import com.vaultflow.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Page<Transaction> findByToWalletId(String toWalletId, Pageable pageable);
    Page<Transaction> findByFromCardId(String fromCardId, Pageable pageable);
    Page<Transaction> findByStatus(String status, Pageable pageable);
    Page<Transaction> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
    Page<Transaction> findByToWalletIdAndCreatedAtBetween(String toWalletId, LocalDateTime from, LocalDateTime to, Pageable pageable);
}
