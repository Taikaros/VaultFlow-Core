package com.vaultflow.service;

import com.vaultflow.dto.TransactionRequest;
import com.vaultflow.model.Card;
import com.vaultflow.model.Transaction;
import com.vaultflow.model.Wallet;
import com.vaultflow.repository.CardRepository;
import com.vaultflow.repository.TransactionRepository;
import com.vaultflow.repository.WalletRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final WalletRepository walletRepository;
    private final NotificationService notificationService;
    private final CurrencyConverter currencyConverter;

    public TransactionService(TransactionRepository transactionRepository,
                              CardRepository cardRepository,
                              WalletRepository walletRepository,
                              NotificationService notificationService,
                              CurrencyConverter currencyConverter) {
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
        this.walletRepository = walletRepository;
        this.notificationService = notificationService;
        this.currencyConverter = currencyConverter;
    }

    public Page<Transaction> listAll(String status, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        if (status != null) {
            return transactionRepository.findByStatus(status, pageable);
        }
        if (from != null && to != null) {
            return transactionRepository.findByCreatedAtBetween(from, to, pageable);
        }
        return transactionRepository.findAll(pageable);
    }

    public Page<Transaction> listByWallet(String walletId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        if (from != null && to != null) {
            return transactionRepository.findByToWalletIdAndCreatedAtBetween(walletId, from, to, pageable);
        }
        return transactionRepository.findByToWalletId(walletId, pageable);
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

        String fromCurrency = fromWallet.getCurrency();
        String toCurrency = toWallet.getCurrency();
        double convertedAmount = currencyConverter.convert(request.amount(), fromCurrency, toCurrency);
        Double rate = fromCurrency.equals(toCurrency) ? null : currencyConverter.getRate(fromCurrency);

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
        transaction.setOriginalAmount(request.amount());
        transaction.setOriginalCurrency(fromCurrency);
        transaction.setConversionRate(rate);
        transaction = transactionRepository.save(transaction);

        try {
            fromWallet.setBalance(fromWallet.getBalance() - request.amount());
            toWallet.setBalance(toWallet.getBalance() + convertedAmount);
            card.setSpentAmount(card.getSpentAmount() + request.amount());
            walletRepository.save(fromWallet);
            walletRepository.save(toWallet);
            cardRepository.save(card);
            transaction.setStatus("COMPLETED");

            if (toWallet.getCompanyId() != null) {
                notificationService.sendTransactionNotification(
                    toWallet.getCompanyId(),
                    "PAYMENT_RECEIVED",
                    "Pago recibido: " + String.format("%.2f %s", convertedAmount, toCurrency),
                    java.util.Map.of("transactionId", transaction.getId(), "amount", convertedAmount, "currency", toCurrency)
                );
            }
        } catch (Exception e) {
            transaction.setStatus("FAILED");
        }

        return transactionRepository.save(transaction);
    }
}
