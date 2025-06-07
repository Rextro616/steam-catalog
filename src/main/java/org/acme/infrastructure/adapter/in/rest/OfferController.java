package org.acme.infrastructure.adapter.in.rest;

import org.acme.application.port.in.OfferUseCase;
import org.acme.domain.model.Offer;
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

@Path("/api/offers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Offers", description = "Gestión de ofertas y descuentos estacionales")
@Slf4j
public class OfferController {

    @Inject
    OfferUseCase offerUseCase;

    @GET
    @Path("/active")
    @Operation(
            summary = "Obtener ofertas activas",
            description = "Obtiene todas las ofertas que están actualmente vigentes"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Ofertas activas obtenidas exitosamente",
                    content = @Content(schema = @Schema(implementation = Offer.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response getActiveOffers() {
        try {
            log.info("GET /api/offers/active");

            List<Offer> offers = offerUseCase.getActiveOffers();

            log.info("Found {} active offers", offers.size());
            return Response.ok(offers).build();

        } catch (Exception e) {
            log.error("Internal error in getActiveOffers", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener ofertas activas")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/seasonal")
    @Operation(
            summary = "Obtener ofertas estacionales",
            description = "Obtiene las ofertas especiales de temporada (rebajas de verano, invierno, etc.)"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Ofertas estacionales obtenidas exitosamente",
                    content = @Content(schema = @Schema(implementation = Offer.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response getSeasonalOffers() {
        try {
            log.info("GET /api/offers/seasonal");

            List<Offer> offers = offerUseCase.getSeasonalOffers();

            log.info("Found {} seasonal offers", offers.size());
            return Response.ok(offers).build();

        } catch (Exception e) {
            log.error("Internal error in getSeasonalOffers", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener ofertas estacionales")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Operation(
            summary = "Obtener oferta por ID",
            description = "Obtiene los detalles de una oferta específica"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Oferta encontrada",
                    content = @Content(schema = @Schema(implementation = Offer.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Oferta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response getOfferById(
            @Parameter(description = "ID de la oferta", required = true, example = "summer-sale-2025")
            @PathParam("id") String id) {
        try {
            log.info("GET /api/offers/{}", id);

            Offer offer = offerUseCase.getOfferById(id);

            log.info("Offer found: {}", offer.getName());
            return Response.ok(offer).build();

        } catch (IllegalArgumentException e) {
            log.warn("Offer not found: {}", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Oferta no encontrada")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in getOfferById for id: " + id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener oferta")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/type/{type}")
    @Operation(
            summary = "Obtener ofertas por tipo",
            description = "Obtiene ofertas filtradas por su tipo (SEASONAL, WEEKEND, FLASH, etc.)"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Ofertas obtenidas exitosamente",
                    content = @Content(schema = @Schema(implementation = Offer.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Tipo de oferta inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response getOffersByType(
            @Parameter(
                    description = "Tipo de oferta",
                    required = true,
                    example = "SEASONAL",
                    schema = @Schema(enumeration = {"SEASONAL", "WEEKEND", "FLASH", "DAILY", "SPECIAL"})
            )
            @PathParam("type") String type) {
        try {
            log.info("GET /api/offers/type/{}", type);

            // Validate offer type
            if (!isValidOfferType(type)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.builder()
                                .message("Tipo de oferta inválido")
                                .details("Los tipos válidos son: SEASONAL, WEEKEND, FLASH, DAILY, SPECIAL")
                                .build())
                        .build();
            }

            List<Offer> offers = offerUseCase.getOffersByType(type.toUpperCase());

            log.info("Found {} offers of type: {}", offers.size(), type);
            return Response.ok(offers).build();

        } catch (Exception e) {
            log.error("Internal error in getOffersByType for type: " + type, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener ofertas por tipo")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/game/{gameId}")
    @Operation(
            summary = "Obtener ofertas por juego",
            description = "Obtiene todas las ofertas activas para un juego específico"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Ofertas del juego obtenidas exitosamente",
                    content = @Content(schema = @Schema(implementation = Offer.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Juego no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response getOffersByGame(
            @Parameter(description = "ID del juego", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathParam("gameId") String gameId) {
        try {
            log.info("GET /api/offers/game/{}", gameId);

            List<Offer> offers = offerUseCase.getOffersByGame(gameId);

            log.info("Found {} offers for game: {}", offers.size(), gameId);
            return Response.ok(offers).build();

        } catch (IllegalArgumentException e) {
            log.warn("Game not found: {}", gameId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Juego no encontrado")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in getOffersByGame for gameId: " + gameId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener ofertas del juego")
                            .build())
                    .build();
        }
    }

    @POST
    @Operation(
            summary = "Crear nueva oferta",
            description = "Crea una nueva oferta o promoción"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "Oferta creada exitosamente",
                    content = @Content(schema = @Schema(implementation = Offer.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response createOffer(@Valid CreateOfferRequest request) {
        try {
            log.info("POST /api/offers - Creating offer: {}", request.name);

            OfferUseCase.CreateOfferCommand command = OfferUseCase.CreateOfferCommand.builder()
                    .name(request.name)
                    .description(request.description)
                    .gameIds(request.gameIds)
                    .discountPercentage(request.discountPercentage)
                    .startDate(request.startDate)
                    .endDate(request.endDate)
                    .offerType(request.offerType)
                    .build();

            Offer offer = offerUseCase.createOffer(command);

            log.info("Offer created successfully with ID: {}", offer.getId());
            return Response.status(Response.Status.CREATED).entity(offer).build();

        } catch (IllegalArgumentException e) {
            log.warn("Bad request for createOffer: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.builder()
                            .message("Error de validación")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in createOffer", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al crear oferta")
                            .build())
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(
            summary = "Desactivar oferta",
            description = "Desactiva una oferta existente"
    )
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Oferta desactivada exitosamente"),
            @APIResponse(
                    responseCode = "404",
                    description = "Oferta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response deactivateOffer(
            @Parameter(description = "ID de la oferta", required = true)
            @PathParam("id") String id) {
        try {
            log.info("DELETE /api/offers/{}", id);

            offerUseCase.deactivateOffer(id);

            log.info("Offer deactivated successfully: {}", id);
            return Response.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("Offer not found for deactivation: {}", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Oferta no encontrada")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in deactivateOffer for id: " + id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al desactivar oferta")
                            .build())
                    .build();
        }
    }

    // Helper methods
    private boolean isValidOfferType(String type) {
        if (type == null) return false;
        String upperType = type.toUpperCase();
        return upperType.equals("SEASONAL") ||
                upperType.equals("WEEKEND") ||
                upperType.equals("FLASH") ||
                upperType.equals("DAILY") ||
                upperType.equals("SPECIAL");
    }

    // Request DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Datos para crear una nueva oferta")
    public static class CreateOfferRequest {
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 1, max = 255, message = "El nombre debe tener entre 1 y 255 caracteres")
        @Schema(description = "Nombre de la oferta", example = "Rebajas de Verano 2025", required = true)
        public String name;

        @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
        @Schema(description = "Descripción de la oferta", example = "Gran descuento en juegos de acción y aventura")
        public String description;

        @NotNull(message = "La lista de juegos es obligatoria")
        @Size(min = 1, message = "Debe incluir al menos un juego")
        @Schema(description = "Lista de IDs de juegos incluidos en la oferta", example = "[\"game1\", \"game2\"]", required = true)
        public List<String> gameIds;

        @NotNull(message = "El porcentaje de descuento es obligatorio")
        @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
        @DecimalMax(value = "100.0", message = "El descuento no puede ser mayor al 100%")
        @Schema(description = "Porcentaje de descuento", example = "25.0", required = true)
        public Double discountPercentage;

        @NotBlank(message = "La fecha de inicio es obligatoria")
        @Schema(description = "Fecha de inicio en formato ISO", example = "2025-07-01T00:00:00", required = true)
        public String startDate;

        @NotBlank(message = "La fecha de fin es obligatoria")
        @Schema(description = "Fecha de fin en formato ISO", example = "2025-07-31T23:59:59", required = true)
        public String endDate;

        @NotBlank(message = "El tipo de oferta es obligatorio")
        @Pattern(regexp = "^(SEASONAL|WEEKEND|FLASH|DAILY|SPECIAL)$", message = "Tipo de oferta inválido")
        @Schema(description = "Tipo de oferta", example = "SEASONAL", required = true,
                enumeration = {"SEASONAL", "WEEKEND", "FLASH", "DAILY", "SPECIAL"})
        public String offerType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Respuesta de error estándar")
    public static class ErrorResponse {
        @Schema(description = "Mensaje de error", example = "Oferta no encontrada")
        public String message;

        @Schema(description = "Detalles adicionales del error", example = "No existe una oferta con el ID especificado")
        public String details;

        @Schema(description = "Timestamp del error", example = "2025-06-06T10:30:00Z")
        @Builder.Default
        public String timestamp = Instant.now().toString();
    }
}