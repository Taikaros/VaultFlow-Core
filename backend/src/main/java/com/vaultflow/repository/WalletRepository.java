package com.vaultflow.repository;

import com.vaultflow.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, String> {
    List<Wallet> findByCompanyId(String companyId);
}
