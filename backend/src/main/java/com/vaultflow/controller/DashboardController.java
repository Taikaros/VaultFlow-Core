package com.vaultflow.controller;

import com.vaultflow.model.Card;
import com.vaultflow.repository.CardRepository;
import com.vaultflow.repository.TransactionRepository;
import com.vaultflow.repository.WalletRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final WalletRepository walletRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;

    public DashboardController(WalletRepository walletRepository,
                               CardRepository cardRepository,
                               TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics(Authentication auth) {
        String companyId = auth.getName();

        var wallets = walletRepository.findByCompanyId(companyId);
        double totalBalance = wallets.stream().mapToDouble(w -> w.getBalance()).sum();
        long walletCount = wallets.size();

        var cards = cardRepository.findAll();
        long activeCards = cards.stream().filter(c -> "ACTIVE".equals(c.getStatus())).count();
        long totalCards = cards.size();

        var transactions = transactionRepository.findAll();
        long completedTx = transactions.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
        long failedTx = transactions.stream().filter(t -> "FAILED".equals(t.getStatus())).count();

        return ResponseEntity.ok(Map.of(
            "totalBalance", totalBalance,
            "walletCount", walletCount,
            "activeCards", activeCards,
            "totalCards", totalCards,
            "completedTransactions", completedTx,
            "failedTransactions", failedTx,
            "totalTransactions", transactions.size()
        ));
    }
}
