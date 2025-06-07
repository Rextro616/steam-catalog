package org.acme.application.port.in;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acme.domain.model.Gift;

import java.util.List;

public interface GiftUseCase {
    Gift sendGift(SendGiftCommand command);
    Gift claimGift(String giftId, String recipientId);
    List<Gift> getPendingGifts(String recipientId);
    List<Gift> getSentGifts(String senderId);
    void cancelGift(String giftId, String senderId);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class SendGiftCommand {
        public String gameId;
        public String senderId;
        public String recipientId;
        public String message;
        public Double amount;
        public String currency;
    }
}