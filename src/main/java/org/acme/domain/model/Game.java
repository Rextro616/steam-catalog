package org.acme.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.acme.domain.model.valueobjects.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Game {
    private GameId id;
    private String title;
    private String description;
    private String shortDescription;
    private Price price;
    private String developer;
    private String publisher;
    private LocalDateTime releaseDate;
    private Set<Category> categories;
    private Set<String> tags;
    private List<String> images;
    private SystemRequirements systemRequirements;
    private Rating rating;
    private Integer stock;
    @Builder.Default
    private Boolean isActive = true;
    @Builder.Default
    private Boolean isPreOrderAvailable = false;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public boolean isAvailableForPurchase() {
        return isActive && stock > 0 && releaseDate.isBefore(LocalDateTime.now());
    }

    public boolean isAvailableForPreOrder() {
        return isPreOrderAvailable && releaseDate.isAfter(LocalDateTime.now());
    }

    public void updateStock(Integer newStock) {
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock no puede ser negativo");
        }
        this.stock = newStock;
        this.updatedAt = LocalDateTime.now();
    }

    public void applyDiscount(Double discountPercentage) {
        this.price = price.applyDiscount(discountPercentage);
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCategories(Set<Category> categories) {
        this.categories = categories;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateImages(List<String> images) {
        this.images = images;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateSystemRequirements(SystemRequirements systemRequirements) {
        this.systemRequirements = systemRequirements;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRating(Rating rating) {
        this.rating = rating;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePreOrderAvailability(Boolean isPreOrderAvailable) {
        this.isPreOrderAvailable = isPreOrderAvailable;
        this.updatedAt = LocalDateTime.now();
    }
}
