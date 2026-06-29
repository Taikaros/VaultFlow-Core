package com.vaultflow.service;

import com.vaultflow.dto.WalletResponse;
import com.vaultflow.model.Wallet;
import com.vaultflow.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final CurrencyConverter currencyConverter;

    public WalletService(WalletRepository walletRepository, CurrencyConverter currencyConverter) {
        this.walletRepository = walletRepository;
        this.currencyConverter = currencyConverter;
    }

    public List<WalletResponse> listByCompany(String companyId) {
        return walletRepository.findByCompanyId(companyId)
            .stream()
            .map(WalletResponse::from)
            .toList();
    }

    public WalletResponse getById(String walletId) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new IllegalArgumentException("Wallet no encontrada"));
        return WalletResponse.from(wallet);
    }

    @Transactional
    public WalletResponse create(String companyId, String currency) {
        String cur = currency != null ? currency.toUpperCase() : "USD";
        if (!currencyConverter.isSupported(cur)) {
            throw new IllegalArgumentException("Moneda no soportada: " + cur);
        }
        Wallet wallet = new Wallet();
        wallet.setCompanyId(companyId);
        wallet.setCurrency(cur);
        wallet.setBalance(0.0);
        wallet = walletRepository.save(wallet);
        return WalletResponse.from(wallet);
    }
}
