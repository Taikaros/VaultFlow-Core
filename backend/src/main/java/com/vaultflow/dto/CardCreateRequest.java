package com.vaultflow.dto;

import jakarta.validation.constraints.NotBlank;

public record CardCreateRequest(
    @NotBlank String holderName,
    Double limitAmount
) {}
