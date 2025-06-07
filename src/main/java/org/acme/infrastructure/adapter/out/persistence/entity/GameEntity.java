package org.acme.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;

@Entity
@Table(name = "games")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class GameEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "developer", nullable = false, length = 255)
    private String developer;

    @Column(name = "publisher", nullable = false, length = 255)
    private String publisher;

    @Column(name = "release_date", nullable = false)
    private LocalDateTime releaseDate;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "game_categories",
            joinColumns = @JoinColumn(name = "game_id")
    )
    @Column(name = "category_name")
    private Set<String> categories;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "game_tags",
            joinColumns = @JoinColumn(name = "game_id")
    )
    @Column(name = "tag_name")
    private Set<String> tags;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "game_images",
            joinColumns = @JoinColumn(name = "game_id")
    )
    @Column(name = "image_url", length = 500)
    @OrderColumn(name = "image_order")
    private List<String> images;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "minimumOS", column = @Column(name = "min_os")),
            @AttributeOverride(name = "minimumProcessor", column = @Column(name = "min_processor")),
            @AttributeOverride(name = "minimumMemory", column = @Column(name = "min_memory")),
            @AttributeOverride(name = "minimumGraphics", column = @Column(name = "min_graphics")),
            @AttributeOverride(name = "minimumStorage", column = @Column(name = "min_storage")),
            @AttributeOverride(name = "recommendedOS", column = @Column(name = "rec_os")),
            @AttributeOverride(name = "recommendedProcessor", column = @Column(name = "rec_processor")),
            @AttributeOverride(name = "recommendedMemory", column = @Column(name = "rec_memory")),
            @AttributeOverride(name = "recommendedGraphics", column = @Column(name = "rec_graphics")),
            @AttributeOverride(name = "recommendedStorage", column = @Column(name = "rec_storage"))
    })
    private SystemRequirementsEmbeddable systemRequirements;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "rating_value", precision = 3, scale = 2)),
            @AttributeOverride(name = "totalVotes", column = @Column(name = "rating_votes"))
    })
    private RatingEmbeddable rating;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_pre_order_available")
    @Builder.Default
    private Boolean isPreOrderAvailable = false;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemRequirementsEmbeddable {
        @Column(name = "min_os", length = 255)
        private String minimumOS;

        @Column(name = "min_processor", length = 255)
        private String minimumProcessor;

        @Column(name = "min_memory", length = 100)
        private String minimumMemory;

        @Column(name = "min_graphics", length = 255)
        private String minimumGraphics;

        @Column(name = "min_storage", length = 100)
        private String minimumStorage;

        @Column(name = "rec_os", length = 255)
        private String recommendedOS;

        @Column(name = "rec_processor", length = 255)
        private String recommendedProcessor;

        @Column(name = "rec_memory", length = 100)
        private String recommendedMemory;

        @Column(name = "rec_graphics", length = 255)
        private String recommendedGraphics;

        @Column(name = "rec_storage", length = 100)
        private String recommendedStorage;
    }

    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingEmbeddable {
        @Column(name = "rating_value", precision = 3, scale = 2)
        private BigDecimal value;

        @Column(name = "rating_votes")
        private Integer totalVotes;
    }
}