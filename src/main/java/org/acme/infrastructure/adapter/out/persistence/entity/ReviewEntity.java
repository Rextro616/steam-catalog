package org.acme.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ReviewEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "game_id", nullable = false, length = 36)
    private String gameId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_recommended", nullable = false)
    private Boolean isRecommended;

    @Column(name = "helpful_votes")
    @Builder.Default
    private Integer helpfulVotes = 0;

    @Column(name = "total_votes")
    @Builder.Default
    private Integer totalVotes = 0;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Index(name = "idx_review_game_id")
    @Column(name = "game_id", insertable = false, updatable = false)
    private String gameIdIndex;

    @Index(name = "idx_review_user_id")
    @Column(name = "user_id", insertable = false, updatable = false)
    private String userIdIndex;
}
