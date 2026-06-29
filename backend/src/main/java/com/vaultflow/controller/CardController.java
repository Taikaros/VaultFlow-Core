package com.vaultflow.controller;

import com.vaultflow.dto.CardCreateRequest;
import com.vaultflow.model.Card;
import com.vaultflow.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/cards")
    public ResponseEntity<List<Card>> list(@RequestParam(required = false) String walletId) {
        if (walletId != null) {
            return ResponseEntity.ok(cardService.listByWallet(walletId));
        }
        return ResponseEntity.ok(cardService.listAll());
    }

    @PostMapping("/wallets/{walletId}/cards")
    public ResponseEntity<Card> create(@PathVariable String walletId,
                                        @Valid @RequestBody CardCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(cardService.create(walletId, request));
    }

    @GetMapping("/cards/{cardId}")
    public ResponseEntity<Card> getById(@PathVariable String cardId) {
        return ResponseEntity.ok(cardService.getById(cardId));
    }

    @PatchMapping("/cards/{cardId}")
    public ResponseEntity<Card> update(@PathVariable String cardId,
                                        @RequestBody Map<String, Object> body) {
        String status = (String) body.get("status");
        Double limit = body.get("limitAmount") != null
            ? ((Number) body.get("limitAmount")).doubleValue() : null;
        return ResponseEntity.ok(cardService.update(cardId, status, limit));
    }

    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> cancel(@PathVariable String cardId) {
        cardService.cancel(cardId);
        return ResponseEntity.noContent().build();
    }
}
