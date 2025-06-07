package org.acme.infrastructure.adapter.out.external;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.acme.application.port.out.NotificationServicePort;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
@Slf4j
public class NotificationServiceAdapter implements NotificationServicePort {

    @Inject
    @RestClient
    NotificationServiceClient notificationServiceClient;

    @Override
    public void sendGiftNotification(GiftNotificationRequest request) {
        try {
            log.info("Sending gift notification to user: {}", request.recipientId);

            NotificationServiceClient.GiftNotificationDto dto = NotificationServiceClient.GiftNotificationDto.builder()
                    .recipientId(request.recipientId)
                    .senderName(request.senderName)
                    .gameName(request.gameName)
                    .message(request.message)
                    .giftId(request.giftId)
                    .build();

            notificationServiceClient.sendGiftNotification(dto);

        } catch (Exception e) {
            log.error("Error sending gift notification to user: {}", request.recipientId, e);
            // Non-critical error, continue execution
        }
    }

    @Override
    public void sendPreOrderConfirmation(PreOrderNotificationRequest request) {
        try {
            log.info("Sending pre-order confirmation to user: {}", request.userId);

            NotificationServiceClient.PreOrderNotificationDto dto = NotificationServiceClient.PreOrderNotificationDto.builder()
                    .userId(request.userId)
                    .gameName(request.gameName)
                    .releaseDate(request.releaseDate)
                    .bonusContent(request.bonusContent)
                    .preOrderId(request.preOrderId)
                    .build();

            notificationServiceClient.sendPreOrderConfirmation(dto);

        } catch (Exception e) {
            log.error("Error sending pre-order confirmation to user: {}", request.userId, e);
            // Non-critical error, continue execution
        }
    }

    @Override
    public void sendGameReleaseNotification(GameReleaseNotificationRequest request) {
        try {
            log.info("Sending game release notification to user: {}", request.userId);

            NotificationServiceClient.GameReleaseNotificationDto dto = NotificationServiceClient.GameReleaseNotificationDto.builder()
                    .userId(request.userId)
                    .gameName(request.gameName)
                    .downloadLink(request.downloadLink)
                    .releaseNotes(request.releaseNotes)
                    .build();

            notificationServiceClient.sendGameReleaseNotification(dto);

        } catch (Exception e) {
            log.error("Error sending game release notification to user: {}", request.userId, e);
            // Non-critical error, continue execution
        }
    }

    @Override
    public void sendOfferNotification(OfferNotificationRequest request) {
        try {
            log.info("Sending offer notification to user: {}", request.userId);

            NotificationServiceClient.OfferNotificationDto dto = NotificationServiceClient.OfferNotificationDto.builder()
                    .userId(request.userId)
                    .offerName(request.offerName)
                    .discountPercentage(request.discountPercentage)
                    .validUntil(request.validUntil)
                    .gameNames(request.gameNames)
                    .build();

            notificationServiceClient.sendOfferNotification(dto);

        } catch (Exception e) {
            log.error("Error sending offer notification to user: {}", request.userId, e);
            // Non-critical error, continue execution
        }
    }

    @Override
    public void sendReviewNotification(ReviewNotificationRequest request) {
        try {
            log.info("Sending review notification to publisher: {}", request.publisherId);

            NotificationServiceClient.ReviewNotificationDto dto = NotificationServiceClient.ReviewNotificationDto.builder()
                    .publisherId(request.publisherId)
                    .gameName(request.gameName)
                    .reviewerName(request.reviewerName)
                    .reviewContent(request.reviewContent)
                    .isRecommended(request.isRecommended)
                    .reviewId(request.reviewId)
                    .build();

            notificationServiceClient.sendReviewNotification(dto);

        } catch (Exception e) {
            log.error("Error sending review notification to publisher: {}", request.publisherId, e);
            // Non-critical error, continue execution
        }
    }

    @RegisterRestClient(configKey = "notification-service")
    public interface NotificationServiceClient {

        @POST
        @Path("/notifications/gift")
        @Consumes(MediaType.APPLICATION_JSON)
        void sendGiftNotification(GiftNotificationDto request);

        @POST
        @Path("/notifications/preorder")
        @Consumes(MediaType.APPLICATION_JSON)
        void sendPreOrderConfirmation(PreOrderNotificationDto request);

        @POST
        @Path("/notifications/release")
        @Consumes(MediaType.APPLICATION_JSON)
        void sendGameReleaseNotification(GameReleaseNotificationDto request);

        @POST
        @Path("/notifications/offer")
        @Consumes(MediaType.APPLICATION_JSON)
        void sendOfferNotification(OfferNotificationDto request);

        @POST
        @Path("/notifications/review")
        @Consumes(MediaType.APPLICATION_JSON)
        void sendReviewNotification(ReviewNotificationDto request);

        // DTOs for external communication
        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        class GiftNotificationDto {
            public String recipientId;
            public String senderName;
            public String gameName;
            public String message;
            public String giftId;
        }

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        class PreOrderNotificationDto {
            public String userId;
            public String gameName;
            public String releaseDate;
            public String bonusContent;
            public String preOrderId;
        }

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        class GameReleaseNotificationDto {
            public String userId;
            public String gameName;
            public String downloadLink;
            public String releaseNotes;
        }

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        class OfferNotificationDto {
            public String userId;
            public String offerName;
            public String discountPercentage;
            public String validUntil;
            public List<String> gameNames;
        }

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        class ReviewNotificationDto {
            public String publisherId;
            public String gameName;
            public String reviewerName;
            public String reviewContent;
            public Boolean isRecommended;
            public String reviewId;
        }
    }
}
