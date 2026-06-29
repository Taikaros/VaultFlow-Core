package com.vaultflow.dto;

import com.vaultflow.model.Company;
import com.vaultflow.model.Wallet;

public record WalletResponse(String id, String companyId, Double balance, String currency) {
    public static WalletResponse from(Wallet w) {
        return new WalletResponse(w.getId(), w.getCompanyId(), w.getBalance(), w.getCurrency());
    }
}
