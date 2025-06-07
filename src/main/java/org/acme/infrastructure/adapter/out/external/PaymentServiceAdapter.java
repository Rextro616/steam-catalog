package org.acme.infrastructure.adapter.out.external;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.acme.application.port.out.PaymentServicePort;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
@Slf4j
public class PaymentServiceAdapter implements PaymentServicePort {

    @Inject
    @RestClient
    PaymentServiceClient paymentServiceClient;

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        try {
            log.info("Processing payment for user: {} amount: {} {}",
                    request.userId, request.amount, request.currency);

            PaymentServiceClient.PaymentRequestDto dto = PaymentServiceClient.PaymentRequestDto.builder()
                    .userId(request.userId)
                    .amount(request.amount)
                    .currency(request.currency)
                    .gameId(request.gameId)
                    .paymentMethod(request.paymentMethod)
                    .description(request.description)
                    .build();

            PaymentServiceClient.PaymentResultDto result = paymentServiceClient.processPayment(dto);

            return PaymentResult.builder()
                    .paymentId(result.paymentId)
                    .status(result.status)
                    .transactionId(result.transactionId)
                    .errorMessage(result.errorMessage)
                    .processedAt(result.processedAt)
                    .build();

        } catch (Exception e) {
            log.error("Error processing payment for user: {}", request.userId, e);
            return PaymentServicePort.PaymentResult.builder()
                    .status("FAILED")
                    .errorMessage("Error en el procesamiento del pago: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public void refundPayment(String paymentId) {
        try {
            log.info("Processing refund for payment: {}", paymentId);
            paymentServiceClient.refundPayment(paymentId);
        } catch (Exception e) {
            log.error("Error processing refund for payment: {}", paymentId, e);
            throw new RuntimeException("Error al procesar reembolso", e);
        }
    }

    @Override
    public PaymentStatus getPaymentStatus(String paymentId) {
        try {
            log.debug("Fetching payment status: {}", paymentId);
            PaymentServiceClient.PaymentStatusDto dto = paymentServiceClient.getPaymentStatus(paymentId);

            return PaymentStatus.builder()
                    .paymentId(dto.paymentId)
                    .status(dto.status)
                    .amount(dto.amount)
                    .currency(dto.currency)
                    .createdAt(dto.createdAt)
                    .updatedAt(dto.updatedAt)
                    .build();

        } catch (Exception e) {
            log.error("Error fetching payment status: {}", paymentId, e);
            throw new IllegalArgumentException("Estado de pago no encontrado: " + paymentId);
        }
    }

    @RegisterRestClient(configKey = "payment-service")
    public interface PaymentServiceClient {

        @POST
        @Path("/payments")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        PaymentResultDto processPayment(PaymentRequestDto request);

        @POST
        @Path("/payments/{paymentId}/refund")
        @Produces(MediaType.APPLICATION_JSON)
        void refundPayment(@PathParam("paymentId") String paymentId);

        @GET
        @Path("/payments/{paymentId}/status")
        @Produces(MediaType.APPLICATION_JSON)
        PaymentStatusDto getPaymentStatus(@PathParam("paymentId") String paymentId);

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        class PaymentRequestDto {
            public String userId;
            public java.math.BigDecimal amount;
            public String currency;
            public String gameId;
            public String paymentMethod;
            public String description;
        }

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        class PaymentResultDto {
            public String paymentId;
            public String status;
            public String transactionId;
            public String errorMessage;
            public java.time.LocalDateTime processedAt;
        }

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        class PaymentStatusDto {
            public String paymentId;
            public String status;
            public java.math.BigDecimal amount;
            public String currency;
            public java.time.LocalDateTime createdAt;
            public java.time.LocalDateTime updatedAt;
        }
    }
}
