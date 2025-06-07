package org.acme.infrastructure.adapter.in.rest;

import org.acme.application.port.in.PreOrderUseCase;
import org.acme.domain.model.PreOrder;
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

@Path("/api/preorders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Pre-Orders", description = "Gestión de reservas de juegos antes del lanzamiento")
@Slf4j
public class PreOrderController {

    @Inject
    PreOrderUseCase preOrderUseCase;

    @POST
    @Operation(
            summary = "Crear pre-orden",
            description = "Reserva un juego antes de su lanzamiento oficial"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "Pre-orden creada exitosamente",
                    content = @Content(schema = @Schema(implementation = PreOrder.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "409",
                    description = "El juego no está disponible para pre-orden",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response createPreOrder(@Valid CreatePreOrderRequest request) {
        try {
            log.info("POST /api/preorders - Creating pre-order for game: {} by user: {}",
                    request.gameId, request.userId);

            PreOrderUseCase.CreatePreOrderCommand command = PreOrderUseCase.CreatePreOrderCommand.builder()
                    .gameId(request.gameId)
                    .userId(request.userId)
                    .amount(request.amount)
                    .currency(request.currency)
                    .bonusContent(request.bonusContent)
                    .build();

            PreOrder preOrder = preOrderUseCase.createPreOrder(command);

            log.info("Pre-order created successfully with ID: {}", preOrder.getId());
            return Response.status(Response.Status.CREATED).entity(preOrder).build();

        } catch (IllegalArgumentException e) {
            log.warn("Bad request for createPreOrder: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.builder()
                            .message("Error de validación")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (IllegalStateException e) {
            log.warn("Conflict in createPreOrder: {}", e.getMessage());
            return Response.status(Response.Status.CONFLICT)
                    .entity(ErrorResponse.builder()
                            .message("Conflicto en pre-orden")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in createPreOrder", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al crear pre-orden")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/user/{userId}")
    @Operation(
            summary = "Obtener pre-órdenes del usuario",
            description = "Obtiene todas las pre-órdenes realizadas por un usuario específico"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Pre-órdenes obtenidas exitosamente",
                    content = @Content(schema = @Schema(implementation = PreOrder.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response getUserPreOrders(
            @Parameter(description = "ID del usuario", required = true, example = "user123")
            @PathParam("userId") String userId) {
        try {
            log.info("GET /api/preorders/user/{}", userId);

            List<PreOrder> preOrders = preOrderUseCase.getPreOrdersByUser(userId);

            log.info("Found {} pre-orders for user: {}", preOrders.size(), userId);
            return Response.ok(preOrders).build();

        } catch (Exception e) {
            log.error("Internal error in getUserPreOrders for user: " + userId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener pre-órdenes")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/{preOrderId}")
    @Operation(
            summary = "Obtener pre-orden por ID",
            description = "Obtiene los detalles de una pre-orden específica"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Pre-orden encontrada",
                    content = @Content(schema = @Schema(implementation = PreOrder.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Pre-orden no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response getPreOrderById(
            @Parameter(description = "ID de la pre-orden", required = true)
            @PathParam("preOrderId") String preOrderId) {
        try {
            log.info("GET /api/preorders/{}", preOrderId);

            PreOrder preOrder = preOrderUseCase.getPreOrderById(preOrderId);

            log.info("Pre-order found: {}", preOrderId);
            return Response.ok(preOrder).build();

        } catch (IllegalArgumentException e) {
            log.warn("Pre-order not found: {}", preOrderId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Pre-orden no encontrada")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in getPreOrderById for id: " + preOrderId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener pre-orden")
                            .build())
                    .build();
        }
    }

    @DELETE
    @Path("/{preOrderId}/user/{userId}")
    @Operation(
            summary = "Cancelar pre-orden",
            description = "Cancela una pre-orden existente del usuario"
    )
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Pre-orden cancelada exitosamente"),
            @APIResponse(
                    responseCode = "404",
                    description = "Pre-orden no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "403",
                    description = "No autorizado para cancelar esta pre-orden",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response cancelPreOrder(
            @Parameter(description = "ID de la pre-orden", required = true)
            @PathParam("preOrderId") String preOrderId,

            @Parameter(description = "ID del usuario", required = true)
            @PathParam("userId") String userId) {
        try {
            log.info("DELETE /api/preorders/{}/user/{}", preOrderId, userId);

            preOrderUseCase.cancelPreOrder(preOrderId, userId);

            log.info("Pre-order cancelled successfully: {}", preOrderId);
            return Response.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("Pre-order not found for cancellation: {}", preOrderId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Pre-orden no encontrada")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (SecurityException e) {
            log.warn("Unauthorized cancellation attempt for pre-order: {} by user: {}", preOrderId, userId);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ErrorResponse.builder()
                            .message("No autorizado")
                            .details("No puedes cancelar esta pre-orden")
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in cancelPreOrder for id: " + preOrderId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al cancelar pre-orden")
                            .build())
                    .build();
        }
    }

    @POST
    @Path("/{preOrderId}/complete")
    @Operation(
            summary = "Completar pre-orden",
            description = "Marca una pre-orden como completada cuando el juego es lanzado"
    )
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Pre-orden completada exitosamente"),
            @APIResponse(
                    responseCode = "404",
                    description = "Pre-orden no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "409",
                    description = "La pre-orden no puede ser completada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response completePreOrder(
            @Parameter(description = "ID de la pre-orden", required = true)
            @PathParam("preOrderId") String preOrderId) {
        try {
            log.info("POST /api/preorders/{}/complete", preOrderId);

            preOrderUseCase.completePreOrder(preOrderId);

            log.info("Pre-order completed successfully: {}", preOrderId);
            return Response.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("Pre-order not found for completion: {}", preOrderId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Pre-orden no encontrada")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (IllegalStateException e) {
            log.warn("Pre-order cannot be completed: {}", preOrderId);
            return Response.status(Response.Status.CONFLICT)
                    .entity(ErrorResponse.builder()
                            .message("La pre-orden no puede ser completada")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in completePreOrder for id: " + preOrderId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al completar pre-orden")
                            .build())
                    .build();
        }
    }

    // Request DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Datos para crear una pre-orden")
    public static class CreatePreOrderRequest {
        @NotBlank(message = "El ID del juego es obligatorio")
        @Schema(description = "ID del juego a reservar", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
        public String gameId;

        @NotBlank(message = "El ID del usuario es obligatorio")
        @Schema(description = "ID del usuario que realiza la reserva", example = "user123", required = true)
        public String userId;

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.0", message = "El monto no puede ser negativo")
        @Schema(description = "Monto pagado por la pre-orden", example = "59.99", required = true)
        public Double amount;

        @NotBlank(message = "La moneda es obligatoria")
        @Pattern(regexp = "^[A-Z]{3}$", message = "La moneda debe ser un código de 3 letras")
        @Schema(description = "Código de moneda ISO", example = "USD", required = true)
        public String currency;

        @Size(max = 1000, message = "El contenido bonus no puede exceder 1000 caracteres")
        @Schema(description = "Contenido bonus incluido", example = "Early access + exclusive weapon skin")
        public String bonusContent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Respuesta de error estándar")
    public static class ErrorResponse {
        @Schema(description = "Mensaje de error", example = "Pre-orden no encontrada")
        public String message;

        @Schema(description = "Detalles adicionales del error", example = "No existe una pre-orden con el ID especificado")
        public String details;

        @Schema(description = "Timestamp del error", example = "2025-06-06T10:30:00Z")
        @Builder.Default
        public String timestamp = Instant.now().toString();
    }
}