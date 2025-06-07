package org.acme.application.port.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface AnalyticsServicePort {
    void trackGameView(GameViewEvent event);
    void trackGamePurchase(GamePurchaseEvent event);
    void trackSearchQuery(SearchQueryEvent event);
    void trackOfferClick(OfferClickEvent event);
    AnalyticsReport getGameAnalytics(String gameId, String period);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class GameViewEvent {
        public String userId;
        public String gameId;
        public String source; // search, category, recommendation
        public LocalDateTime timestamp;
        public String sessionId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class GamePurchaseEvent {
        public String userId;
        public String gameId;
        public BigDecimal price;
        public String currency;
        public String paymentMethod;
        public LocalDateTime timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class SearchQueryEvent {
        public String userId;
        public String query;
        public Integer resultsCount;
        public String category;
        public LocalDateTime timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class OfferClickEvent {
        public String userId;
        public String offerId;
        public String gameId;
        public String offerType;
        public LocalDateTime timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class AnalyticsReport {
        public String gameId;
        public Integer views;
        public Integer purchases;
        public BigDecimal revenue;
        public Double conversionRate;
        public String period;
        public LocalDateTime generatedAt;
    }
}
