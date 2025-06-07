package org.acme.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.acme.domain.model.valueobjects.GameId;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Bundle {
    private String id;
    private String name;
    private String description;
    private Set<GameId> gameIds;
    private Price originalPrice;
    private Price bundlePrice;
    private Double discountPercentage;
    @Builder.Default
    private Boolean isActive = true;
    private LocalDateTime validUntil;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public Bundle(String id, String name, String description, Set<GameId> gameIds,
                  Price originalPrice, Price bundlePrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.gameIds = gameIds;
        this.originalPrice = originalPrice;
        this.bundlePrice = bundlePrice;
        this.discountPercentage = calculateDiscountPercentage();
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    private Double calculateDiscountPercentage() {
        if (originalPrice.getAmount().compareTo(bundlePrice.getAmount()) > 0) {
            return (1 - bundlePrice.getAmount().doubleValue() / originalPrice.getAmount().doubleValue()) * 100;
        }
        return 0.0;
    }

    public boolean isValidBundle() {
        return isActive && (validUntil == null || validUntil.isAfter(LocalDateTime.now()));
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void extendValidity(LocalDateTime newValidUntil) {
        this.validUntil = newValidUntil;
    }
}
