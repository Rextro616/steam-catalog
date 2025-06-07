package org.acme.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.acme.domain.model.valueobjects.GameId;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SalesStatistics {
    private String id;
    private GameId gameId;
    private String publisherId; // External domain reference
    @Builder.Default
    private Integer totalSales = 0;
    @Builder.Default
    private Integer monthlySales = 0;
    @Builder.Default
    private Integer weeklySales = 0;
    @Builder.Default
    private Integer dailySales = 0;
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal monthlyRevenue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;
    @Builder.Default
    private Integer totalReviews = 0;
    @Builder.Default
    private Integer positiveReviews = 0;
    @Builder.Default
    private LocalDateTime lastUpdated = LocalDateTime.now();

    public void updateSales(Integer newSales, BigDecimal revenue) {
        if (newSales < 0) {
            throw new IllegalArgumentException("Las ventas no pueden ser negativas");
        }
        if (revenue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Los ingresos no pueden ser negativos");
        }

        this.totalSales += newSales;
        this.dailySales += newSales;
        this.weeklySales += newSales;
        this.monthlySales += newSales;
        this.totalRevenue = this.totalRevenue.add(revenue);
        this.monthlyRevenue = this.monthlyRevenue.add(revenue);
        this.lastUpdated = LocalDateTime.now();
    }

    public void updateReviews(Boolean isPositive, BigDecimal rating) {
        this.totalReviews++;
        if (isPositive) {
            this.positiveReviews++;
        }
        this.averageRating = rating;
        this.lastUpdated = LocalDateTime.now();
    }

    public Double getPositiveReviewPercentage() {
        return totalReviews > 0 ? (double) positiveReviews / totalReviews * 100 : 0.0;
    }

    public BigDecimal getAverageRevenuePerSale() {
        return totalSales > 0 ? totalRevenue.divide(BigDecimal.valueOf(totalSales), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
    }

    public void resetDailyStats() {
        this.dailySales = 0;
        this.lastUpdated = LocalDateTime.now();
    }

    public void resetWeeklyStats() {
        this.weeklySales = 0;
        this.lastUpdated = LocalDateTime.now();
    }

    public void resetMonthlyStats() {
        this.monthlySales = 0;
        this.monthlyRevenue = BigDecimal.ZERO;
        this.lastUpdated = LocalDateTime.now();
    }

    public boolean hasRecentActivity() {
        return lastUpdated.isAfter(LocalDateTime.now().minusDays(7));
    }

    public Double getSalesGrowthRate() {
        return monthlySales > 0 ? (double) dailySales / monthlySales * 30 : 0.0;
    }
}