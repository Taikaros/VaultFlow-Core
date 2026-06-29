package com.vaultflow.service;

import com.vaultflow.dto.TransactionRequest;
import com.vaultflow.model.Card;
import com.vaultflow.model.Transaction;
import com.vaultflow.model.Wallet;
import com.vaultflow.repository.CardRepository;
import com.vaultflow.repository.TransactionRepository;
import com.vaultflow.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final WalletRepository walletRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              CardRepository cardRepository,
                              WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
        this.walletRepository = walletRepository;
    }

    public List<Transaction> listByWallet(String walletId) {
        return transactionRepository.findByToWalletId(walletId);
    }

    public List<Transaction> listAll(String status, LocalDateTime from, LocalDateTime to) {
        if (status != null) {
            return transactionRepository.findByStatus(status);
        }
        if (from != null && to != null) {
            return transactionRepository.findByCreatedAtBetween(from, to);
        }
        return transactionRepository.findAll();
    }

    @Transactional
    public Transaction pay(TransactionRequest request) {
        Card card = cardRepository.findById(request.fromCardId())
            .orElseThrow(() -> new IllegalArgumentException("Tarjeta no encontrada"));

        if (!"ACTIVE".equals(card.getStatus())) {
            throw new IllegalStateException("La tarjeta no está activa");
        }

        Wallet fromWallet = walletRepository.findById(card.getWalletId())
            .orElseThrow(() -> new IllegalArgumentException("Wallet origen no encontrada"));

        Wallet toWallet = walletRepository.findById(request.toWalletId())
            .orElseThrow(() -> new IllegalArgumentException("Wallet destino no encontrada"));

        if (fromWallet.getBalance() < request.amount()) {
            throw new IllegalStateException("Fondos insuficientes");
        }

        if (card.getSpentAmount() + request.amount() > card.getLimitAmount()) {
            throw new IllegalStateException("Límite de tarjeta excedido");
        }

        Transaction transaction = new Transaction();
        transaction.setFromCardId(request.fromCardId());
        transaction.setToWalletId(request.toWalletId());
        transaction.setAmount(request.amount());
        transaction.setDescription(request.description());
        transaction.setType("PAYMENT");
        transaction.setStatus("PENDING");
        transaction = transactionRepository.save(transaction);

        try {
            fromWallet.setBalance(fromWallet.getBalance() - request.amount());
            toWallet.setBalance(toWallet.getBalance() + request.amount());
            card.setSpentAmount(card.getSpentAmount() + request.amount());
            walletRepository.save(fromWallet);
            walletRepository.save(toWallet);
            cardRepository.save(card);
            transaction.setStatus("COMPLETED");
        } catch (Exception e) {
            transaction.setStatus("FAILED");
        }

        return transactionRepository.save(transaction);
    }
}
