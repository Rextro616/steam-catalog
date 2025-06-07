package org.acme.application.service;

import org.acme.application.port.in.GiftUseCase;
import org.acme.application.port.out.*;
import org.acme.domain.model.Gift;
import org.acme.domain.model.Game;
import org.acme.domain.model.valueobjects.*;
import org.acme.domain.repository.GameRepository;
import org.acme.domain.repository.GiftRepository;
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
public class GiftApplicationService implements GiftUseCase {

    @Inject
    GiftRepository giftRepository;

    @Inject
    GameRepository gameRepository;

    @Inject
    UserServicePort userServicePort;

    @Inject
    PaymentServicePort paymentServicePort;

    @Inject
    NotificationServicePort notificationService;

    @Inject
    InventoryServicePort inventoryServicePort;

    @Override
    public Gift sendGift(SendGiftCommand command) {
        log.info("Sending gift of game: {} from: {} to: {}", command.gameId, command.senderId, command.recipientId);

        validateSendGiftCommand(command);

        GameId gameId = new GameId(command.gameId);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado: " + command.gameId));

        if (!userServicePort.userExists(command.senderId)) {
            throw new IllegalArgumentException("Remitente no encontrado: " + command.senderId);
        }

        if (!userServicePort.userExists(command.recipientId)) {
            throw new IllegalArgumentException("Destinatario no encontrado: " + command.recipientId);
        }

        if (inventoryServicePort.userOwnsGame(command.recipientId, command.gameId)) {
            throw new IllegalStateException("El destinatario ya posee este juego");
        }

        if (command.senderId.equals(command.recipientId)) {
            throw new IllegalArgumentException("No puedes enviarte un regalo a ti mismo");
        }

        if (!userServicePort.areUsersFriends(command.senderId, command.recipientId)) {
            log.warn("Users are not friends, but allowing gift - sender: {}, recipient: {}",
                    command.senderId, command.recipientId);
        }

        Price amount = new Price(BigDecimal.valueOf(command.amount), command.currency);
        PaymentServicePort.PaymentRequest paymentRequest = PaymentServicePort.PaymentRequest.builder()
                .userId(command.senderId)
                .amount(amount.getAmount())
                .currency(amount.getCurrency())
                .gameId(command.gameId)
                .paymentMethod("CREDIT_CARD")
                .description("Regalo de " + game.getTitle())
                .build();

        PaymentServicePort.PaymentResult paymentResult = paymentServicePort.processPayment(paymentRequest);

        if (!"SUCCESS".equals(paymentResult.status)) {
            throw new IllegalStateException("Error en el pago: " + paymentResult.errorMessage);
        }

        Gift gift = Gift.builder()
                .id(UUID.randomUUID().toString())
                .gameId(gameId)
                .senderId(command.senderId)
                .recipientId(command.recipientId)
                .message(command.message)
                .amount(amount)
                .expirationDate(LocalDateTime.now().plusDays(30)) // Gift expires in 30 days
                .build();

        Gift savedGift = giftRepository.save(gift);

        UserServicePort.UserDto sender = userServicePort.getUserById(command.senderId);

        notificationService.sendGiftNotification(
                NotificationServicePort.GiftNotificationRequest.builder()
                        .recipientId(command.recipientId)
                        .senderName(sender.displayName != null ? sender.displayName : sender.username)
                        .gameName(game.getTitle())
                        .message(command.message)
                        .giftId(savedGift.getId())
                        .build()
        );

        log.info("Gift sent successfully with ID: {}", savedGift.getId());
        return savedGift;
    }

    @Override
    public Gift claimGift(String giftId, String recipientId) {
        log.info("Claiming gift: {} by user: {}", giftId, recipientId);

        Gift gift = giftRepository.findById(giftId)
                .orElseThrow(() -> new IllegalArgumentException("Regalo no encontrado: " + giftId));

        if (!gift.getRecipientId().equals(recipientId)) {
            throw new SecurityException("No puedes reclamar este regalo");
        }

        if (!gift.canBeClaimed()) {
            throw new IllegalStateException("Este regalo no puede ser reclamado: " + gift.getStatus());
        }

        if (inventoryServicePort.userOwnsGame(recipientId, gift.getGameId().getValue())) {
            throw new IllegalStateException("Ya posees este juego");
        }

        gift.claim();
        Gift savedGift = giftRepository.save(gift);

        Game game = gameRepository.findById(gift.getGameId())
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));

        inventoryServicePort.addGameToLibrary(
                InventoryServicePort.AddGameRequest.builder()
                        .userId(recipientId)
                        .gameId(gift.getGameId().getValue())
                        .gameName(game.getTitle())
                        .acquisitionType("GIFT")
                        .transactionId(giftId)
                        .acquiredAt(LocalDateTime.now())
                        .build()
        );

        log.info("Gift claimed successfully: {} by user: {}", giftId, recipientId);
        return savedGift;
    }

    @Override
    public List<Gift> getPendingGifts(String recipientId) {
        log.debug("Fetching pending gifts for user: {}", recipientId);

        if (!userServicePort.userExists(recipientId)) {
            throw new IllegalArgumentException("Usuario no encontrado: " + recipientId);
        }

        return giftRepository.findPendingByRecipientId(recipientId);
    }

    @Override
    public List<Gift> getSentGifts(String senderId) {
        log.debug("Fetching sent gifts for user: {}", senderId);

        if (!userServicePort.userExists(senderId)) {
            throw new IllegalArgumentException("Usuario no encontrado: " + senderId);
        }

        return giftRepository.findBySenderId(senderId);
    }

    @Override
    public void cancelGift(String giftId, String senderId) {
        log.info("Cancelling gift: {} by user: {}", giftId, senderId);

        Gift gift = giftRepository.findById(giftId)
                .orElseThrow(() -> new IllegalArgumentException("Regalo no encontrado: " + giftId));

        if (!gift.getSenderId().equals(senderId)) {
            throw new SecurityException("No puedes cancelar este regalo");
        }

        if (!gift.canBeCancelled()) {
            throw new IllegalStateException("Este regalo no puede ser cancelado: " + gift.getStatus());
        }

        gift.cancel();
        giftRepository.save(gift);

        log.info("Gift cancelled successfully: {}", giftId);
    }

    public void expireOldGifts() {
        log.info("Processing expired gifts");

        List<Gift> expiredGifts = giftRepository.findExpiredGifts();

        for (Gift gift : expiredGifts) {
            if (gift.isPending()) {
                gift.expire();
                giftRepository.save(gift);

                log.info("Expired gift: {}", gift.getId());
            }
        }

        log.info("Processed {} expired gifts", expiredGifts.size());
    }

    private void validateSendGiftCommand(SendGiftCommand command) {
        if (command.gameId == null || command.gameId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del juego es obligatorio");
        }

        if (command.senderId == null || command.senderId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del remitente es obligatorio");
        }

        if (command.recipientId == null || command.recipientId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del destinatario es obligatorio");
        }

        if (command.amount == null || command.amount <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }

        if (command.currency == null || command.currency.trim().isEmpty()) {
            throw new IllegalArgumentException("La moneda es obligatoria");
        }

        if (command.message != null && command.message.length() > 500) {
            throw new IllegalArgumentException("El mensaje no puede exceder 500 caracteres");
        }
    }
}