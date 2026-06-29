package com.vaultflow.repository;

import com.vaultflow.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, String> {
    List<Card> findByWalletId(String walletId);
}
