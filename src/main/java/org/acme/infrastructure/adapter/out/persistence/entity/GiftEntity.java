package org.acme.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gifts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class GiftEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "game_id", nullable = false, length = 36)
    private String gameId;

    @Column(name = "sender_id", nullable = false, length = 50)
    private String senderId;

    @Column(name = "recipient_id", nullable = false, length = 50)
    private String recipientId;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "sent_date", nullable = false)
    @Builder.Default
    private LocalDateTime sentDate = LocalDateTime.now();

    @Column(name = "claimed_date")
    private LocalDateTime claimedDate;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Index(name = "idx_gift_recipient_id")
    @Column(name = "recipient_id", insertable = false, updatable = false)
    private String recipientIdIndex;

    @Index(name = "idx_gift_sender_id")
    @Column(name = "sender_id", insertable = false, updatable = false)
    private String senderIdIndex;
}
