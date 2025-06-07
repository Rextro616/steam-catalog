package org.acme.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.acme.domain.model.valueobjects.GameId;
import org.acme.domain.model.valueobjects.Price;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Gift {
    private String id;
    private GameId gameId;
    private String senderId; // External domain reference
    private String recipientId; // External domain reference
    private String message;
    private Price amount;
    @Builder.Default
    private LocalDateTime sentDate = LocalDateTime.now();
    private LocalDateTime claimedDate;
    @Builder.Default
    private String status = "PENDING"; // PENDING, CLAIMED, EXPIRED, CANCELLED
    private LocalDateTime expirationDate;

    public void claim() {
        if (!"PENDING".equals(status)) {
            throw new IllegalStateException("El regalo ya fue reclamado o no está disponible");
        }
        if (isExpired()) {
            throw new IllegalStateException("El regalo ha expirado");
        }
        this.status = "CLAIMED";
        this.claimedDate = LocalDateTime.now();
    }

    public void cancel() {
        if (!"PENDING".equals(status)) {
            throw new IllegalStateException("Solo se pueden cancelar regalos pendientes");
        }
        this.status = "CANCELLED";
    }

    public void expire() {
        if ("PENDING".equals(status)) {
            this.status = "EXPIRED";
        }
    }

    public boolean isPending() {
        return "PENDING".equals(status) && !isExpired();
    }

    public boolean isClaimed() {
        return "CLAIMED".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public boolean isExpired() {
        return "EXPIRED".equals(status) ||
                (expirationDate != null && LocalDateTime.now().isAfter(expirationDate));
    }

    public boolean canBeClaimed() {
        return isPending() && !isExpired();
    }

    public boolean canBeCancelled() {
        return isPending() && !isExpired();
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        if (expirationDate.isBefore(sentDate)) {
            throw new IllegalArgumentException("La fecha de expiración no puede ser anterior a la fecha de envío");
        }
        this.expirationDate = expirationDate;
    }

    public long getDaysUntilExpiration() {
        if (expirationDate == null) {
            return -1;
        }
        return java.time.Duration.between(LocalDateTime.now(), expirationDate).toDays();
    }
}