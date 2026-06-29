package com.vaultflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "from_card_id")
    private String fromCardId;

    @Column(name = "to_wallet_id", nullable = false)
    private String toWalletId;

    @Column(nullable = false)
    private Double amount;

    private String description;

    @Column(nullable = false)
    private String type = "PAYMENT";

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "original_amount")
    private Double originalAmount;

    @Column(name = "original_currency")
    private String originalCurrency;

    @Column(name = "conversion_rate")
    private Double conversionRate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFromCardId() { return fromCardId; }
    public void setFromCardId(String fromCardId) { this.fromCardId = fromCardId; }
    public String getToWalletId() { return toWalletId; }
    public void setToWalletId(String toWalletId) { this.toWalletId = toWalletId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getOriginalAmount() { return originalAmount; }
    public void setOriginalAmount(Double originalAmount) { this.originalAmount = originalAmount; }
    public String getOriginalCurrency() { return originalCurrency; }
    public void setOriginalCurrency(String originalCurrency) { this.originalCurrency = originalCurrency; }
    public Double getConversionRate() { return conversionRate; }
    public void setConversionRate(Double conversionRate) { this.conversionRate = conversionRate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
