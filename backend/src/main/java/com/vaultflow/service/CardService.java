package com.vaultflow.service;

import com.vaultflow.dto.CardCreateRequest;
import com.vaultflow.model.Card;
import com.vaultflow.repository.CardRepository;
import com.vaultflow.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final WalletRepository walletRepository;

    public CardService(CardRepository cardRepository, WalletRepository walletRepository) {
        this.cardRepository = cardRepository;
        this.walletRepository = walletRepository;
    }

    public List<Card> listByWallet(String walletId) {
        return cardRepository.findByWalletId(walletId);
    }

    public List<Card> listAll() {
        return cardRepository.findAll();
    }

    public Card getById(String cardId) {
        return cardRepository.findById(cardId)
            .orElseThrow(() -> new IllegalArgumentException("Tarjeta no encontrada"));
    }

    @Transactional
    public Card create(String walletId, CardCreateRequest request) {
        walletRepository.findById(walletId)
            .orElseThrow(() -> new IllegalArgumentException("Wallet no encontrada"));

        Card card = new Card();
        card.setWalletId(walletId);
        card.setHolderName(request.holderName());
        card.setCardNumber(generateCardNumber());
        if (request.limitAmount() != null) {
            card.setLimitAmount(request.limitAmount());
        }
        return cardRepository.save(card);
    }

    @Transactional
    public Card update(String cardId, String status, Double limitAmount) {
        Card card = getById(cardId);
        if (status != null) card.setStatus(status);
        if (limitAmount != null) card.setLimitAmount(limitAmount);
        return cardRepository.save(card);
    }

    @Transactional
    public void cancel(String cardId) {
        Card card = getById(cardId);
        card.setStatus("CANCELLED");
        cardRepository.save(card);
    }

    private String generateCardNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("4");
        for (int i = 0; i < 15; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
