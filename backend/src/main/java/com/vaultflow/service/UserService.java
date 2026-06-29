package com.vaultflow.service;

import com.vaultflow.model.User;
import com.vaultflow.model.Wallet;
import com.vaultflow.repository.UserRepository;
import com.vaultflow.repository.WalletRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       WalletRepository walletRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<User> listByCompany(String companyId, Pageable pageable) {
        return userRepository.findByCompanyId(companyId, pageable);
    }

    public User getById(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Transactional
    public User create(String companyId, String name, String email, String password, String role, String walletId) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        if (walletId != null) {
            walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet no encontrada"));
        }

        User user = new User();
        user.setCompanyId(companyId);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role != null ? role : "EMPLOYEE");
        user.setWalletId(walletId);
        return userRepository.save(user);
    }

    @Transactional
    public User update(String userId, String name, String role, String walletId) {
        User user = getById(userId);
        if (name != null) user.setName(name);
        if (role != null) user.setRole(role);
        if (walletId != null) user.setWalletId(walletId);
        return userRepository.save(user);
    }

    @Transactional
    public void delete(String userId) {
        userRepository.deleteById(userId);
    }
}
