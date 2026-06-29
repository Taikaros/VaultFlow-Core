package com.vaultflow.controller;

import com.vaultflow.dto.WalletResponse;
import com.vaultflow.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public ResponseEntity<List<WalletResponse>> list(Authentication auth) {
        return ResponseEntity.ok(walletService.listByCompany(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<WalletResponse> create(Authentication auth,
                                                  @RequestBody(required = false) Map<String, String> body) {
        String currency = body != null ? body.get("currency") : null;
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(walletService.create(auth.getName(), currency));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getById(@PathVariable String walletId) {
        return ResponseEntity.ok(walletService.getById(walletId));
    }
}
