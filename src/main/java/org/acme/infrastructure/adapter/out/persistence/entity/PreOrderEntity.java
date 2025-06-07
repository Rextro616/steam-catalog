package org.acme.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pre_orders", indexes = {
        @Index(name = "idx_preorder_user_id", columnList = "user_id"),
        @Index(name = "idx_preorder_game_id", columnList = "game_id"),
        @Index(name = "idx_preorder_user_game", columnList = "user_id, game_id") // Composite index
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class PreOrderEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "game_id", nullable = false, length = 36)
    private String gameId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "paid_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "pre_order_date", nullable = false)
    @Builder.Default
    private LocalDateTime preOrderDate = LocalDateTime.now();

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "CONFIRMED";

    @Column(name = "bonus_content", columnDefinition = "TEXT")
    private String bonusContent;

    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;

    @Column(name = "user_id", insertable = false, updatable = false)
    private String userIdIndex;

    @Column(name = "game_id", insertable = false, updatable = false)
    private String gameIdIndex;
}
