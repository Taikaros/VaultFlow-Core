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

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
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
        Wallet wallet = new Wallet();
        wallet.setCompanyId(companyId);
        if (currency != null) wallet.setCurrency(currency);
        wallet.setBalance(0.0);
        wallet = walletRepository.save(wallet);
        return WalletResponse.from(wallet);
    }
}
