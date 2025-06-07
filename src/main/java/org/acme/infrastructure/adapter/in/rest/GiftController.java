package org.acme.infrastructure.adapter.in.rest;

import org.acme.application.port.in.GiftUseCase;
import org.acme.domain.model.Gift;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import java.time.Instant;
import java.util.List;

@Path("/api/gifts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Gifts", description = "Sistema de regalos de juegos entre usuarios")
@Slf4j
public class GiftController {

    @Inject
    GiftUseCase giftUseCase;

    @POST
    @Operation(
            summary = "Enviar regalo",
            description = "Envía un juego como regalo a otro usuario"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "Regalo enviado exitosamente",
                    content = @Content(schema = @Schema(implementation = Gift.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Juego o usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "409",
                    description = "El destinatario ya posee el juego",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response sendGift(@Valid SendGiftRequest request) {
        try {
            log.info("POST /api/gifts - Sending gift of game: {} from: {} to: {}",
                    request.gameId, request.senderId, request.recipientId);

            GiftUseCase.SendGiftCommand command = GiftUseCase.SendGiftCommand.builder()
                    .gameId(request.gameId)
                    .senderId(request.senderId)
                    .recipientId(request.recipientId)
                    .message(request.message)
                    .amount(request.amount)
                    .currency(request.currency)
                    .build();

            Gift gift = giftUseCase.sendGift(command);

            log.info("Gift sent successfully with ID: {}", gift.getId());
            return Response.status(Response.Status.CREATED).entity(gift).build();

        } catch (IllegalArgumentException e) {
            log.warn("Bad request for sendGift: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.builder()
                            .message("Error de validación")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            log.warn("Resource not found for sendGift: {}", e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Recurso no encontrado")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (IllegalStateException e) {
            log.warn("Conflict in sendGift: {}", e.getMessage());
            return Response.status(Response.Status.CONFLICT)
                    .entity(ErrorResponse.builder()
                            .message("Conflicto al enviar regalo")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in sendGift", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al enviar regalo")
                            .build())
                    .build();
        }
    }

    @POST
    @Path("/{giftId}/claim/{recipientId}")
    @Operation(
            summary = "Reclamar regalo",
            description = "Reclama un regalo pendiente"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Regalo reclamado exitosamente",
                    content = @Content(schema = @Schema(implementation = Gift.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Regalo no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "409",
                    description = "El regalo ya fue reclamado o no está disponible",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "403",
                    description = "No autorizado para reclamar este regalo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response claimGift(
            @Parameter(description = "ID del regalo", required = true)
            @PathParam("giftId") String giftId,

            @Parameter(description = "ID del destinatario", required = true)
            @PathParam("recipientId") String recipientId) {
        try {
            log.info("POST /api/gifts/{}/claim/{}", giftId, recipientId);

            Gift gift = giftUseCase.claimGift(giftId, recipientId);

            log.info("Gift claimed successfully: {}", giftId);
            return Response.ok(gift).build();

        } catch (IllegalArgumentException e) {
            log.warn("Gift not found: {}", giftId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Regalo no encontrado")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (IllegalStateException e) {
            log.warn("Gift cannot be claimed: {}", giftId);
            return Response.status(Response.Status.CONFLICT)
                    .entity(ErrorResponse.builder()
                            .message("Conflicto al reclamar regalo")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (SecurityException e) {
            log.warn("Unauthorized claim attempt for gift: {} by user: {}", giftId, recipientId);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ErrorResponse.builder()
                            .message("No autorizado")
                            .details("No puedes reclamar este regalo")
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in claimGift for id: " + giftId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al reclamar regalo")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/pending/{recipientId}")
    @Operation(
            summary = "Obtener regalos pendientes",
            description = "Obtiene todos los regalos pendientes de un usuario"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Regalos pendientes obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = Gift.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response getPendingGifts(
            @Parameter(description = "ID del destinatario", required = true)
            @PathParam("recipientId") String recipientId) {
        try {
            log.info("GET /api/gifts/pending/{}", recipientId);

            List<Gift> gifts = giftUseCase.getPendingGifts(recipientId);

            log.info("Found {} pending gifts for user: {}", gifts.size(), recipientId);
            return Response.ok(gifts).build();

        } catch (Exception e) {
            log.error("Internal error in getPendingGifts for user: " + recipientId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener regalos pendientes")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/sent/{senderId}")
    @Operation(
            summary = "Obtener regalos enviados",
            description = "Obtiene todos los regalos enviados por un usuario"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Regalos enviados obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = Gift.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response getSentGifts(
            @Parameter(description = "ID del remitente", required = true)
            @PathParam("senderId") String senderId) {
        try {
            log.info("GET /api/gifts/sent/{}", senderId);

            List<Gift> gifts = giftUseCase.getSentGifts(senderId);

            log.info("Found {} sent gifts for user: {}", gifts.size(), senderId);
            return Response.ok(gifts).build();

        } catch (Exception e) {
            log.error("Internal error in getSentGifts for user: " + senderId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener regalos enviados")
                            .build())
                    .build();
        }
    }

    @DELETE
    @Path("/{giftId}/sender/{senderId}")
    @Operation(
            summary = "Cancelar regalo",
            description = "Cancela un regalo pendiente"
    )
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Regalo cancelado exitosamente"),
            @APIResponse(
                    responseCode = "404",
                    description = "Regalo no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "403",
                    description = "No autorizado para cancelar este regalo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "409",
                    description = "El regalo no puede ser cancelado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response cancelGift(
            @Parameter(description = "ID del regalo", required = true)
            @PathParam("giftId") String giftId,

            @Parameter(description = "ID del remitente", required = true)
            @PathParam("senderId") String senderId) {
        try {
            log.info("DELETE /api/gifts/{}/sender/{}", giftId, senderId);

            giftUseCase.cancelGift(giftId, senderId);

            log.info("Gift cancelled successfully: {}", giftId);
            return Response.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("Gift not found for cancellation: {}", giftId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Regalo no encontrado")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (SecurityException e) {
            log.warn("Unauthorized cancellation attempt for gift: {} by user: {}", giftId, senderId);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ErrorResponse.builder()
                            .message("No autorizado")
                            .details("No puedes cancelar este regalo")
                            .build())
                    .build();
        } catch (IllegalStateException e) {
            log.warn("Gift cannot be cancelled: {}", giftId);
            return Response.status(Response.Status.CONFLICT)
                    .entity(ErrorResponse.builder()
                            .message("El regalo no puede ser cancelado")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in cancelGift for id: " + giftId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al cancelar regalo")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/{giftId}")
    @Operation(
            summary = "Obtener regalo por ID",
            description = "Obtiene los detalles de un regalo específico"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Regalo encontrado",
                    content = @Content(schema = @Schema(implementation = Gift.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Regalo no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response getGiftById(
            @Parameter(description = "ID del regalo", required = true)
            @PathParam("giftId") String giftId) {
        try {
            log.info("GET /api/gifts/{}", giftId);

            // Note: This would require implementing getGiftById in the use case
            // Gift gift = giftUseCase.getGiftById(giftId);

            // For now, returning a placeholder response
            return Response.status(Response.Status.NOT_IMPLEMENTED)
                    .entity(ErrorResponse.builder()
                            .message("Funcionalidad no implementada")
                            .details("El método getGiftById aún no está implementado")
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("Internal error in getGiftById for id: " + giftId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener regalo")
                            .build())
                    .build();
        }
    }

    // Request DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Datos para enviar un regalo")
    public static class SendGiftRequest {
        @NotBlank(message = "El ID del juego es obligatorio")
        @Schema(description = "ID del juego a regalar", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
        public String gameId;

        @NotBlank(message = "El ID del remitente es obligatorio")
        @Schema(description = "ID del usuario que envía el regalo", example = "user123", required = true)
        public String senderId;

        @NotBlank(message = "El ID del destinatario es obligatorio")
        @Schema(description = "ID del usuario que recibe el regalo", example = "friend456", required = true)
        public String recipientId;

        @Size(max = 500, message = "El mensaje no puede exceder 500 caracteres")
        @Schema(description = "Mensaje personalizado para el regalo", example = "¡Feliz cumpleaños! Espero que disfrutes este juego")
        public String message;

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.0", message = "El monto no puede ser negativo")
        @Schema(description = "Monto del regalo", example = "29.99", required = true)
        public Double amount;

        @NotBlank(message = "La moneda es obligatoria")
        @Pattern(regexp = "^[A-Z]{3}$", message = "La moneda debe ser un código de 3 letras")
        @Schema(description = "Código de moneda ISO", example = "USD", required = true)
        public String currency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Respuesta de error estándar")
    public static class ErrorResponse {
        @Schema(description = "Mensaje de error", example = "Regalo no encontrado")
        public String message;

        @Schema(description = "Detalles adicionales del error", example = "No existe un regalo con el ID especificado")
        public String details;

        @Schema(description = "Timestamp del error", example = "2025-06-06T10:30:00Z")
        @Builder.Default
        public String timestamp = Instant.now().toString();
    }
}