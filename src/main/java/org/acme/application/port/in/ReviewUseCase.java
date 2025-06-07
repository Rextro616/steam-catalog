package org.acme.application.port.in;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acme.domain.model.Review;

import java.util.List;

public interface ReviewUseCase {
    Review createReview(CreateReviewCommand command);
    List<Review> getGameReviews(String gameId, int page, int size);
    List<Review> getUserReviews(String userId, int page, int size);
    void voteReview(String reviewId, Boolean isHelpful);
    List<Review> getMostHelpfulReviews(String gameId, int limit);
    void deleteReview(String reviewId, String userId);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CreateReviewCommand {
        public String gameId;
        public String userId;
        public String content;
        public Boolean isRecommended;
    }
}