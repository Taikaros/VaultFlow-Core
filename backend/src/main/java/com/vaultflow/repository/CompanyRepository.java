package com.vaultflow.repository;

import com.vaultflow.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, String> {
    Optional<Company> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByTaxId(String taxId);
}
