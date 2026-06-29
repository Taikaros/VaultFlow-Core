package com.vaultflow.service;

import com.vaultflow.dto.TransactionRequest;
import com.vaultflow.model.Card;
import com.vaultflow.model.Transaction;
import com.vaultflow.model.Wallet;
import com.vaultflow.repository.CardRepository;
import com.vaultflow.repository.TransactionRepository;
import com.vaultflow.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CurrencyConverter currencyConverter;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, cardRepository, walletRepository, notificationService, currencyConverter);
    }

    @Test
    void pay_shouldCompleteSuccessfully() {
        Card card = new Card();
        card.setId("card1");
        card.setWalletId("w1");
        card.setStatus("ACTIVE");
        card.setLimitAmount(1000.0);
        card.setSpentAmount(0.0);

        Wallet fromWallet = new Wallet();
        fromWallet.setId("w1");
        fromWallet.setBalance(500.0);

        Wallet toWallet = new Wallet();
        toWallet.setId("w2");
        toWallet.setBalance(100.0);

        when(currencyConverter.convert(200.0, "USD", "USD")).thenReturn(200.0);
        when(cardRepository.findById("card1")).thenReturn(Optional.of(card));
        when(walletRepository.findById("w1")).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById("w2")).thenReturn(Optional.of(toWallet));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId("tx-1");
            return t;
        });

        TransactionRequest request = new TransactionRequest("card1", "w2", 200.0, "Test payment");
        Transaction result = transactionService.pay(request);

        assertEquals("COMPLETED", result.getStatus());
        assertEquals(300.0, fromWallet.getBalance(), 0.01);
        assertEquals(300.0, toWallet.getBalance(), 0.01);
        assertEquals(200.0, card.getSpentAmount(), 0.01);
    }

    @Test
    void pay_shouldThrowWhenCardNotActive() {
        Card card = new Card();
        card.setId("card1");
        card.setStatus("SUSPENDED");

        when(cardRepository.findById("card1")).thenReturn(Optional.of(card));

        assertThrows(IllegalStateException.class,
            () -> transactionService.pay(new TransactionRequest("card1", "w2", 100.0, null)));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void pay_shouldThrowWhenInsufficientFunds() {
        Card card = new Card();
        card.setId("card1");
        card.setWalletId("w1");
        card.setStatus("ACTIVE");
        card.setLimitAmount(1000.0);
        card.setSpentAmount(0.0);

        Wallet fromWallet = new Wallet();
        fromWallet.setId("w1");
        fromWallet.setBalance(50.0);

        Wallet toWallet = new Wallet();
        toWallet.setId("w2");
        toWallet.setBalance(0.0);

        when(currencyConverter.convert(100.0, "USD", "USD")).thenReturn(100.0);
        when(cardRepository.findById("card1")).thenReturn(Optional.of(card));
        when(walletRepository.findById("w1")).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById("w2")).thenReturn(Optional.of(toWallet));

        assertThrows(IllegalStateException.class,
            () -> transactionService.pay(new TransactionRequest("card1", "w2", 100.0, null)));
    }

    @Test
    void pay_shouldThrowWhenCardLimitExceeded() {
        Card card = new Card();
        card.setId("card1");
        card.setWalletId("w1");
        card.setStatus("ACTIVE");
        card.setLimitAmount(100.0);
        card.setSpentAmount(80.0);

        Wallet fromWallet = new Wallet();
        fromWallet.setId("w1");
        fromWallet.setBalance(1000.0);

        Wallet toWallet = new Wallet();
        toWallet.setId("w2");
        toWallet.setBalance(0.0);

        when(currencyConverter.convert(50.0, "USD", "USD")).thenReturn(50.0);
        when(cardRepository.findById("card1")).thenReturn(Optional.of(card));
        when(walletRepository.findById("w1")).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById("w2")).thenReturn(Optional.of(toWallet));

        assertThrows(IllegalStateException.class,
            () -> transactionService.pay(new TransactionRequest("card1", "w2", 50.0, null)));
    }

    @Test
    void pay_shouldThrowWhenCardNotFound() {
        when(cardRepository.findById("invalid")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
            () -> transactionService.pay(new TransactionRequest("invalid", "w2", 100.0, null)));
    }

    @Test
    void pay_shouldThrowWhenWalletNotFound() {
        Card card = new Card();
        card.setId("card1");
        card.setWalletId("w1");
        card.setStatus("ACTIVE");

        when(cardRepository.findById("card1")).thenReturn(Optional.of(card));
        when(walletRepository.findById("w1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> transactionService.pay(new TransactionRequest("card1", "w2", 100.0, null)));
    }
}
