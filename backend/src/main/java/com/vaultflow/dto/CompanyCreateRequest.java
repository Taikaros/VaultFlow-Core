package com.vaultflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CompanyCreateRequest(
    @NotBlank String name,
    @NotBlank String taxId,
    @Email @NotBlank String email,
    @NotBlank String password
) {}
