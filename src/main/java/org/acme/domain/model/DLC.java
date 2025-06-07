package org.acme.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.acme.domain.model.valueobjects.GameId;
import org.acme.domain.model.valueobjects.Price;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DLC {
    private String id;
    private String name;
    private String description;
    private GameId baseGameId;
    private Price price;
    @Builder.Default
    private LocalDateTime releaseDate = LocalDateTime.now();
    @Builder.Default
    private Boolean isActive = true;
    private String contentType; // Expansion, Cosmetic, Season Pass, etc.

    public boolean isAvailableForPurchase() {
        return isActive && releaseDate.isBefore(LocalDateTime.now());
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void updatePrice(Price newPrice) {
        this.price = newPrice;
    }

    public boolean isExpansion() {
        return "Expansion".equalsIgnoreCase(contentType);
    }

    public boolean isCosmetic() {
        return "Cosmetic".equalsIgnoreCase(contentType);
    }

    public boolean isSeasonPass() {
        return "Season Pass".equalsIgnoreCase(contentType);
    }
}