package org.acme.application.port.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public interface NotificationServicePort {
    void sendGiftNotification(GiftNotificationRequest request);
    void sendPreOrderConfirmation(PreOrderNotificationRequest request);
    void sendGameReleaseNotification(GameReleaseNotificationRequest request);
    void sendOfferNotification(OfferNotificationRequest request);
    void sendReviewNotification(ReviewNotificationRequest request);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class GiftNotificationRequest {
        public String recipientId;
        public String senderName;
        public String gameName;
        public String message;
        public String giftId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PreOrderNotificationRequest {
        public String userId;
        public String gameName;
        public String releaseDate;
        public String bonusContent;
        public String preOrderId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class GameReleaseNotificationRequest {
        public String userId;
        public String gameName;
        public String downloadLink;
        public String releaseNotes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class OfferNotificationRequest {
        public String userId;
        public String offerName;
        public String discountPercentage;
        public String validUntil;
        public List<String> gameNames;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ReviewNotificationRequest {
        public String publisherId;
        public String gameName;
        public String reviewerName;
        public String reviewContent;
        public Boolean isRecommended;
        public String reviewId;
    }
}
