package com.vaultflow.service;

import com.vaultflow.dto.AuthResponse;
import com.vaultflow.dto.CompanyCreateRequest;
import com.vaultflow.model.Company;
import com.vaultflow.model.Wallet;
import com.vaultflow.repository.CompanyRepository;
import com.vaultflow.repository.WalletRepository;
import com.vaultflow.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(companyRepository, walletRepository, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void register_shouldCreateCompanyAndWalletAndReturnToken() {
        var request = new CompanyCreateRequest("TestCorp", "TAX123", "test@corp.com", "pass123");

        when(companyRepository.existsByEmail("test@corp.com")).thenReturn(false);
        when(companyRepository.existsByTaxId("TAX123")).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenAnswer(i -> {
            Company c = i.getArgument(0);
            c.setId("company-uuid");
            return c;
        });
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtTokenProvider.generateToken("company-uuid", "test@corp.com")).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        verify(companyRepository).save(any(Company.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void register_shouldThrowWhenEmailExists() {
        var request = new CompanyCreateRequest("TestCorp", "TAX123", "test@corp.com", "pass123");
        when(companyRepository.existsByEmail("test@corp.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnTokenWhenCredentialsAreValid() {
        Company company = new Company();
        company.setId("company-uuid");
        company.setEmail("test@corp.com");
        company.setPassword(passwordEncoder.encode("pass123"));

        when(companyRepository.findByEmail("test@corp.com")).thenReturn(Optional.of(company));
        when(jwtTokenProvider.generateToken("company-uuid", "test@corp.com")).thenReturn("jwt-token");

        AuthResponse response = authService.login("test@corp.com", "pass123");

        assertEquals("jwt-token", response.token());
    }

    @Test
    void login_shouldThrowWhenPasswordIsInvalid() {
        Company company = new Company();
        company.setPassword(passwordEncoder.encode("correct-pass"));

        when(companyRepository.findByEmail("test@corp.com")).thenReturn(Optional.of(company));

        assertThrows(IllegalArgumentException.class, () -> authService.login("test@corp.com", "wrong-pass"));
    }

    @Test
    void login_shouldThrowWhenEmailNotFound() {
        when(companyRepository.findByEmail("unknown@corp.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login("unknown@corp.com", "pass"));
    }
}
