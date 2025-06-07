package org.acme.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_statistics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class SalesStatisticsEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "game_id", nullable = false, length = 36, unique = true)
    private String gameId;

    @Column(name = "publisher_id", nullable = false, length = 50)
    private String publisherId;

    @Column(name = "total_sales")
    @Builder.Default
    private Integer totalSales = 0;

    @Column(name = "monthly_sales")
    @Builder.Default
    private Integer monthlySales = 0;

    @Column(name = "weekly_sales")
    @Builder.Default
    private Integer weeklySales = 0;

    @Column(name = "daily_sales")
    @Builder.Default
    private Integer dailySales = 0;

    @Column(name = "total_revenue", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "monthly_revenue", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal monthlyRevenue = BigDecimal.ZERO;

    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "positive_reviews")
    @Builder.Default
    private Integer positiveReviews = 0;

    @Column(name = "last_updated", nullable = false)
    @Builder.Default
    private LocalDateTime lastUpdated = LocalDateTime.now();

    @Index(name = "idx_stats_game_id")
    @Column(name = "game_id", insertable = false, updatable = false)
    private String gameIdIndex;

    @Index(name = "idx_stats_publisher_id")
    @Column(name = "publisher_id", insertable = false, updatable = false)
    private String publisherIdIndex;
}
