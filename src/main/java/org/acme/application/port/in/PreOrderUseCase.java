package org.acme.application.port.in;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acme.domain.model.PreOrder;

import java.util.List;

public interface PreOrderUseCase {
    PreOrder createPreOrder(CreatePreOrderCommand command);
    PreOrder getPreOrderById(String id);
    List<PreOrder> getPreOrdersByUser(String userId);
    void cancelPreOrder(String preOrderId, String userId);
    void completePreOrder(String preOrderId);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CreatePreOrderCommand {
        public String gameId;
        public String userId;
        public Double amount;
        public String currency;
        public String bonusContent;
    }
}
