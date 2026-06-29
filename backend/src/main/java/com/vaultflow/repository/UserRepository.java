package com.vaultflow.repository;

import com.vaultflow.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Page<User> findByCompanyId(String companyId, Pageable pageable);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
