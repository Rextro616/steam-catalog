package org.acme.application.service;

import org.acme.application.port.in.PreOrderUseCase;
import org.acme.application.port.out.*;
import org.acme.domain.model.PreOrder;
import org.acme.domain.model.Game;
import org.acme.domain.model.valueobjects.*;
import org.acme.domain.repository.GameRepository;
import org.acme.domain.repository.PreOrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
@Transactional
@Slf4j
public class PreOrderApplicationService implements PreOrderUseCase {

    @Inject
    PreOrderRepository preOrderRepository;

    @Inject
    GameRepository gameRepository;

    @Inject
    UserServicePort userServicePort;

    @Inject
    PaymentServicePort paymentServicePort;

    @Inject
    NotificationServicePort notificationService;

    @Override
    public PreOrder createPreOrder(CreatePreOrderCommand command) {
        log.info("Creating pre-order for game: {} by user: {}", command.gameId, command.userId);

        validateCreatePreOrderCommand(command);

        GameId gameId = new GameId(command.gameId);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado: " + command.gameId));

        if (!game.isAvailableForPreOrder()) {
            throw new IllegalStateException("El juego no está disponible para reserva");
        }

        if (!userServicePort.userExists(command.userId)) {
            throw new IllegalArgumentException("Usuario no encontrado: " + command.userId);
        }

        if (preOrderRepository.existsByUserAndGame(command.userId, gameId)) {
            throw new IllegalStateException("Ya tienes una pre-orden para este juego");
        }

        Price amount = new Price(BigDecimal.valueOf(command.amount), command.currency);
        PaymentServicePort.PaymentRequest paymentRequest = PaymentServicePort.PaymentRequest.builder()
                .userId(command.userId)
                .amount(amount.getAmount())
                .currency(amount.getCurrency())
                .gameId(command.gameId)
                .paymentMethod("CREDIT_CARD")
                .description("Pre-orden para " + game.getTitle())
                .build();

        PaymentServicePort.PaymentResult paymentResult = paymentServicePort.processPayment(paymentRequest);

        if (!"SUCCESS".equals(paymentResult.status)) {
            throw new IllegalStateException("Error en el pago: " + paymentResult.errorMessage);
        }

        PreOrder preOrder = PreOrder.builder()
                .id(UUID.randomUUID().toString())
                .gameId(gameId)
                .userId(command.userId)
                .paidAmount(amount)
                .bonusContent(command.bonusContent)
                .estimatedDeliveryDate(game.getReleaseDate())
                .build();

        PreOrder savedPreOrder = preOrderRepository.save(preOrder);

        notificationService.sendPreOrderConfirmation(
                NotificationServicePort.PreOrderNotificationRequest.builder()
                        .userId(command.userId)
                        .gameName(game.getTitle())
                        .releaseDate(game.getReleaseDate().toString())
                        .bonusContent(command.bonusContent)
                        .preOrderId(savedPreOrder.getId())
                        .build()
        );

        log.info("Pre-order created successfully with ID: {}", savedPreOrder.getId());
        return savedPreOrder;
    }

    @Override
    public PreOrder getPreOrderById(String id) {
        log.debug("Fetching pre-order by ID: {}", id);

        return preOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pre-orden no encontrada: " + id));
    }

    @Override
    public List<PreOrder> getPreOrdersByUser(String userId) {
        log.debug("Fetching pre-orders for user: {}", userId);

        if (!userServicePort.userExists(userId)) {
            throw new IllegalArgumentException("Usuario no encontrado: " + userId);
        }

        return preOrderRepository.findByUserId(userId);
    }

    @Override
    public void cancelPreOrder(String preOrderId, String userId) {
        log.info("Cancelling pre-order: {} by user: {}", preOrderId, userId);

        PreOrder preOrder = preOrderRepository.findById(preOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Pre-orden no encontrada: " + preOrderId));

        if (!preOrder.getUserId().equals(userId)) {
            throw new SecurityException("No puedes cancelar esta pre-orden");
        }

        if (!preOrder.canBeCancelled()) {
            throw new IllegalStateException("Esta pre-orden no puede ser cancelada");
        }

        preOrder.cancel();
        preOrderRepository.save(preOrder);

        log.info("Pre-order cancelled successfully: {}", preOrderId);
    }

    @Override
    public void completePreOrder(String preOrderId) {
        log.info("Completing pre-order: {}", preOrderId);

        PreOrder preOrder = preOrderRepository.findById(preOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Pre-orden no encontrada: " + preOrderId));

        if (!preOrder.isConfirmed()) {
            throw new IllegalStateException("Solo se pueden completar pre-órdenes confirmadas");
        }

        Game game = gameRepository.findById(preOrder.getGameId())
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));

        if (game.getReleaseDate().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("El juego aún no ha sido lanzado");
        }

        preOrder.complete();
        preOrderRepository.save(preOrder);

        notificationService.sendGameReleaseNotification(
                NotificationServicePort.GameReleaseNotificationRequest.builder()
                        .userId(preOrder.getUserId())
                        .gameName(game.getTitle())
                        .downloadLink("https://steam.com/download/" + game.getId())
                        .releaseNotes("El juego que pre-ordenaste ya está disponible!")
                        .build()
        );

        log.info("Pre-order completed successfully: {}", preOrderId);
    }

    private void validateCreatePreOrderCommand(CreatePreOrderCommand command) {
        if (command.gameId == null || command.gameId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del juego es obligatorio");
        }

        if (command.userId == null || command.userId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio");
        }

        if (command.amount == null || command.amount <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }

        if (command.currency == null || command.currency.trim().isEmpty()) {
            throw new IllegalArgumentException("La moneda es obligatoria");
        }
    }
}