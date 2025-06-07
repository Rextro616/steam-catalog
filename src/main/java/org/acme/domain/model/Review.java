package org.acme.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acme.domain.model.valueobjects.GameId;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Review {
    private String id;
    private GameId gameId;
    private String userId; // External domain reference
    private String content;
    private Boolean isRecommended;
    @Builder.Default
    private Integer helpfulVotes = 0;
    @Builder.Default
    private Integer totalVotes = 0;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    @Builder.Default
    private Boolean isActive = true;

    public void addVote(Boolean isHelpful) {
        this.totalVotes++;
        if (isHelpful) {
            this.helpfulVotes++;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public Double getHelpfulnessRatio() {
        return totalVotes > 0 ? (double) helpfulVotes / totalVotes : 0.0;
    }

    public void updateContent(String newContent) {
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("El contenido de la reseña no puede estar vacío");
        }
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRecommendation(Boolean isRecommended) {
        this.isRecommended = isRecommended;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isPositive() {
        return isRecommended != null && isRecommended;
    }

    public boolean isHelpful() {
        return getHelpfulnessRatio() > 0.6; // Más del 60% considera útil
    }
}