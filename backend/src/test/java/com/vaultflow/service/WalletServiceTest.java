package com.vaultflow.service;

import com.vaultflow.dto.WalletResponse;
import com.vaultflow.model.Wallet;
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
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletService = new WalletService(walletRepository);
    }

    @Test
    void listByCompany_shouldReturnWallets() {
        Wallet w1 = new Wallet();
        w1.setId("w1");
        w1.setCompanyId("c1");
        w1.setBalance(100.0);
        w1.setCurrency("USD");

        Wallet w2 = new Wallet();
        w2.setId("w2");
        w2.setCompanyId("c1");
        w2.setBalance(200.0);
        w2.setCurrency("USD");

        when(walletRepository.findByCompanyId("c1")).thenReturn(List.of(w1, w2));

        List<WalletResponse> result = walletService.listByCompany("c1");

        assertEquals(2, result.size());
        assertEquals(100.0, result.getFirst().balance());
        assertEquals(200.0, result.get(1).balance());
    }

    @Test
    void getById_shouldReturnWallet() {
        Wallet w = new Wallet();
        w.setId("w1");
        w.setCompanyId("c1");
        w.setBalance(500.0);

        when(walletRepository.findById("w1")).thenReturn(Optional.of(w));

        WalletResponse result = walletService.getById("w1");

        assertEquals("w1", result.id());
        assertEquals(500.0, result.balance());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(walletRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> walletService.getById("invalid"));
    }

    @Test
    void create_shouldCreateWalletWithDefaultValues() {
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> {
            Wallet w = i.getArgument(0);
            w.setId("new-wallet");
            return w;
        });

        WalletResponse result = walletService.create("c1", null);

        assertEquals("new-wallet", result.id());
        assertEquals(0.0, result.balance());
        assertEquals("USD", result.currency());
    }

    @Test
    void create_shouldCreateWalletWithCustomCurrency() {
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> {
            Wallet w = i.getArgument(0);
            w.setId("new-wallet");
            return w;
        });

        WalletResponse result = walletService.create("c1", "EUR");

        assertEquals("EUR", result.currency());
    }
}
