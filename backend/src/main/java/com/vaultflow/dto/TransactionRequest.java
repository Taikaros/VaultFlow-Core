package com.vaultflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TransactionRequest(
    @NotBlank String fromCardId,
    @NotBlank String toWalletId,
    @Positive Double amount,
    String description
) {}
