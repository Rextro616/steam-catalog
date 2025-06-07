package org.acme.application.port.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PaymentServicePort {
    PaymentResult processPayment(PaymentRequest request);
    void refundPayment(String paymentId);
    PaymentStatus getPaymentStatus(String paymentId);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PaymentRequest {
        public String userId;
        public BigDecimal amount;
        public String currency;
        public String gameId;
        public String paymentMethod;
        public String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PaymentResult {
        public String paymentId;
        public String status;
        public String transactionId;
        public String errorMessage;
        public LocalDateTime processedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PaymentStatus {
        public String paymentId;
        public String status;
        public BigDecimal amount;
        public String currency;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
    }
}
