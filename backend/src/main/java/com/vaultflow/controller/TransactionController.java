package com.vaultflow.controller;

import com.vaultflow.dto.TransactionRequest;
import com.vaultflow.model.Transaction;
import com.vaultflow.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transactions")
    public ResponseEntity<Transaction> pay(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(transactionService.pay(request));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to) {
        return ResponseEntity.ok(transactionService.listAll(status, from, to));
    }

    @GetMapping("/wallets/{walletId}/transactions")
    public ResponseEntity<List<Transaction>> listByWallet(@PathVariable String walletId) {
        return ResponseEntity.ok(transactionService.listByWallet(walletId));
    }
}
