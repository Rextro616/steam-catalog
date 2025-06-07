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
public class Offer {
    private String id;
    private String name;
    private String description;
    private Set<GameId> gameIds;
    private Double discountPercentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String offerType; // SEASONAL, WEEKEND, FLASH, etc.
    @Builder.Default
    private Boolean isActive = true;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && now.isAfter(startDate) && now.isBefore(endDate);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }

    public boolean isUpcoming() {
        return LocalDateTime.now().isBefore(startDate);
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void extendDuration(LocalDateTime newEndDate) {
        if (newEndDate.isBefore(startDate)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        this.endDate = newEndDate;
    }

    public void updateDiscount(Double newDiscountPercentage) {
        if (newDiscountPercentage < 0 || newDiscountPercentage > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
        }
        this.discountPercentage = newDiscountPercentage;
    }

    public boolean isSeasonal() {
        return "SEASONAL".equalsIgnoreCase(offerType);
    }

    public boolean isWeekend() {
        return "WEEKEND".equalsIgnoreCase(offerType);
    }

    public boolean isFlash() {
        return "FLASH".equalsIgnoreCase(offerType);
    }

    public boolean isDaily() {
        return "DAILY".equalsIgnoreCase(offerType);
    }

    public long getDurationInHours() {
        return java.time.Duration.between(startDate, endDate).toHours();
    }
}