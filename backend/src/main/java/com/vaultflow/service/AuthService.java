package com.vaultflow.service;

import com.vaultflow.dto.AuthResponse;
import com.vaultflow.dto.CompanyCreateRequest;
import com.vaultflow.model.Company;
import com.vaultflow.model.Wallet;
import com.vaultflow.repository.CompanyRepository;
import com.vaultflow.repository.WalletRepository;
import com.vaultflow.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final CompanyRepository companyRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(CompanyRepository companyRepository,
                       WalletRepository walletRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.companyRepository = companyRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(CompanyCreateRequest request) {
        if (companyRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        if (companyRepository.existsByTaxId(request.taxId())) {
            throw new IllegalArgumentException("El taxId ya está registrado");
        }

        Company company = new Company();
        company.setName(request.name());
        company.setTaxId(request.taxId());
        company.setEmail(request.email());
        company.setPassword(passwordEncoder.encode(request.password()));
        company = companyRepository.save(company);

        Wallet wallet = new Wallet();
        wallet.setCompanyId(company.getId());
        wallet.setBalance(10000.0);
        walletRepository.save(wallet);

        String token = jwtTokenProvider.generateToken(company.getId(), company.getEmail());

        return new AuthResponse(token, company);
    }

    public AuthResponse login(String email, String password) {
        Company company = companyRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!passwordEncoder.matches(password, company.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String token = jwtTokenProvider.generateToken(company.getId(), company.getEmail());
        return new AuthResponse(token, company);
    }
}
