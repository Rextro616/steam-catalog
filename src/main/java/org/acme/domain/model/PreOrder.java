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
public class PreOrder {
    private String id;
    private GameId gameId;
    private String userId; // External domain reference
    private Price paidAmount;
    @Builder.Default
    private LocalDateTime preOrderDate = LocalDateTime.now();
    @Builder.Default
    private String status = "CONFIRMED"; // CONFIRMED, CANCELLED, COMPLETED
    private String bonusContent; // Early access, exclusive items, etc.
    private LocalDateTime estimatedDeliveryDate;

    public void cancel() {
        if (!"CONFIRMED".equals(status)) {
            throw new IllegalStateException("Solo se pueden cancelar preórdenes confirmadas");
        }
        this.status = "CANCELLED";
    }

    public void complete() {
        if (!"CONFIRMED".equals(status)) {
            throw new IllegalStateException("Solo se pueden completar preórdenes confirmadas");
        }
        this.status = "COMPLETED";
    }

    public boolean isConfirmed() {
        return "CONFIRMED".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean canBeCancelled() {
        return isConfirmed() && estimatedDeliveryDate.isAfter(LocalDateTime.now());
    }

    public void updateEstimatedDeliveryDate(LocalDateTime newDate) {
        if (newDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de entrega estimada no puede ser en el pasado");
        }
        this.estimatedDeliveryDate = newDate;
    }

    public void updateBonusContent(String newBonusContent) {
        this.bonusContent = newBonusContent;
    }

    public long getDaysUntilDelivery() {
        if (estimatedDeliveryDate == null) {
            return -1;
        }
        return java.time.Duration.between(LocalDateTime.now(), estimatedDeliveryDate).toDays();
    }
}