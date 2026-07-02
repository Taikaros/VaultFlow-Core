package com.vaultflow.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CurrencyConverter {

    private static final Set<String> SUPPORTED = Set.of("USD", "EUR", "ARS", "BRL", "MXN", "COP", "CLP", "PEN");
    private final Map<String, Double> rates = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        rates.put("USD", 1.0);
        rates.put("EUR", 1.08);
        rates.put("ARS", 0.0011);
        rates.put("BRL", 0.18);
        rates.put("MXN", 0.054);
        rates.put("COP", 0.00024);
        rates.put("CLP", 0.0011);
        rates.put("PEN", 0.27);
    }

    public boolean isSupported(String currency) {
        return SUPPORTED.contains(currency);
    }

    public Set<String> getSupportedCurrencies() {
        return SUPPORTED;
    }

    public double convert(double amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) return amount;
        double usdAmount = amount * rates.getOrDefault(fromCurrency, 1.0);
        double result = usdAmount / rates.getOrDefault(toCurrency, 1.0);
        return BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public double getRate(String currency) {
        return rates.getOrDefault(currency, 1.0);
    }
}
