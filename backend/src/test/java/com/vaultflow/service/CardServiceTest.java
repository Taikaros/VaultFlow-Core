package com.vaultflow.service;

import com.vaultflow.dto.CardCreateRequest;
import com.vaultflow.model.Card;
import com.vaultflow.model.Wallet;
import com.vaultflow.repository.CardRepository;
import com.vaultflow.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private WalletRepository walletRepository;

    private CardService cardService;

    @BeforeEach
    void setUp() {
        cardService = new CardService(cardRepository, walletRepository);
    }

    @Test
    void listByWallet_shouldReturnCards() {
        Card c = new Card();
        c.setId("card1");
        c.setWalletId("w1");

        when(cardRepository.findByWalletId("w1")).thenReturn(List.of(c));

        var result = cardService.listByWallet("w1");
        assertEquals(1, result.size());
    }

    @Test
    void getById_shouldReturnCard() {
        Card c = new Card();
        c.setId("card1");
        c.setHolderName("John");

        when(cardRepository.findById("card1")).thenReturn(Optional.of(c));

        var result = cardService.getById("card1");
        assertEquals("John", result.getHolderName());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(cardRepository.findById("invalid")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> cardService.getById("invalid"));
    }

    @Test
    void create_shouldGenerateCardNumberAndUseDefaultLimit() {
        Wallet w = new Wallet();
        w.setId("w1");

        when(walletRepository.findById("w1")).thenReturn(Optional.of(w));
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> {
            Card c = i.getArgument(0);
            c.setId("new-card");
            return c;
        });

        Card result = cardService.create("w1", new CardCreateRequest("John Doe", null));

        assertNotNull(result.getId());
        assertEquals("John Doe", result.getHolderName());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(1000.0, result.getLimitAmount());
        assertTrue(result.getCardNumber().startsWith("4"));
        assertEquals(16, result.getCardNumber().length());
    }

    @Test
    void create_shouldApplyCustomLimit() {
        Wallet w = new Wallet();
        w.setId("w1");

        when(walletRepository.findById("w1")).thenReturn(Optional.of(w));
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));

        Card result = cardService.create("w1", new CardCreateRequest("Jane", 5000.0));
        assertEquals(5000.0, result.getLimitAmount());
    }

    @Test
    void create_shouldThrowWhenWalletNotFound() {
        when(walletRepository.findById("invalid")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
            () -> cardService.create("invalid", new CardCreateRequest("X", 100.0)));
    }

    @Test
    void update_shouldChangeStatus() {
        Card c = new Card();
        c.setId("card1");
        c.setStatus("ACTIVE");

        when(cardRepository.findById("card1")).thenReturn(Optional.of(c));
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));

        Card result = cardService.update("card1", "SUSPENDED", null);
        assertEquals("SUSPENDED", result.getStatus());
    }

    @Test
    void update_shouldChangeLimit() {
        Card c = new Card();
        c.setId("card1");
        c.setLimitAmount(1000.0);

        when(cardRepository.findById("card1")).thenReturn(Optional.of(c));
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));

        Card result = cardService.update("card1", null, 2000.0);
        assertEquals(2000.0, result.getLimitAmount());
    }

    @Test
    void cancel_shouldSetStatusToCANCELLED() {
        Card c = new Card();
        c.setId("card1");
        c.setStatus("ACTIVE");

        when(cardRepository.findById("card1")).thenReturn(Optional.of(c));
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));

        cardService.cancel("card1");
        assertEquals("CANCELLED", c.getStatus());
        verify(cardRepository).save(c);
    }
}
