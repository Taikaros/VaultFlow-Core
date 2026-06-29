package com.vaultflow.controller;

import com.vaultflow.dto.TransactionRequest;
import com.vaultflow.model.Transaction;
import com.vaultflow.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
    public ResponseEntity<Page<Transaction>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(transactionService.listAll(status, from, to, pageable));
    }

    @GetMapping("/wallets/{walletId}/transactions")
    public ResponseEntity<Page<Transaction>> listByWallet(
            @PathVariable String walletId,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(transactionService.listByWallet(walletId, from, to, pageable));
    }
}
