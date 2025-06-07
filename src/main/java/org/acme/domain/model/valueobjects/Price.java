package org.acme.domain.model.valueobjects;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

@Value
@Builder
public class Price {
    BigDecimal amount;
    String currency;

    public Price(BigDecimal amount, String currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("La moneda no puede ser nula o vacía");
        }

        // Validate currency code
        try {
            Currency.getInstance(currency.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Código de moneda inválido: " + currency);
        }

        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency.toUpperCase();
    }

    public static Price of(double amount, String currency) {
        return new Price(BigDecimal.valueOf(amount), currency);
    }

    public static Price free(String currency) {
        return new Price(BigDecimal.ZERO, currency);
    }

    public Price applyDiscount(Double discountPercentage) {
        if (discountPercentage < 0 || discountPercentage > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
        }
        BigDecimal discount = BigDecimal.valueOf(discountPercentage / 100);
        BigDecimal discountedAmount = amount.multiply(BigDecimal.ONE.subtract(discount));
        return new Price(discountedAmount, currency);
    }

    public Price add(Price other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("No se pueden sumar precios con diferentes monedas");
        }
        return new Price(this.amount.add(other.amount), this.currency);
    }

    public Price subtract(Price other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("No se pueden restar precios con diferentes monedas");
        }
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El resultado no puede ser negativo");
        }
        return new Price(result, this.currency);
    }

    public Price multiply(int factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("El factor no puede ser negativo");
        }
        return new Price(amount.multiply(BigDecimal.valueOf(factor)), currency);
    }

    public boolean isFree() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isGreaterThan(Price other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("No se pueden comparar precios con diferentes monedas");
        }
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Price other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("No se pueden comparar precios con diferentes monedas");
        }
        return this.amount.compareTo(other.amount) < 0;
    }

    public String getFormattedPrice() {
        return String.format("%.2f %s", amount, currency);
    }

    @Override
    public String toString() {
        return getFormattedPrice();
    }
}