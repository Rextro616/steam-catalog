package org.acme.infrastructure.adapter.in.rest;

import org.acme.application.port.in.PublisherUseCase;
import org.acme.domain.model.Game;
import org.acme.domain.model.SalesStatistics;
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

@Path("/api/publisher")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Publishers", description = "Funcionalidades para publishers y desarrolladores")
@Slf4j
public class PublisherController {

    @Inject
    PublisherUseCase publisherUseCase;

    @POST
    @Path("/games")
    @Operation(
            summary = "Publicar juego",
            description = "Publica un nuevo juego en la tienda como publisher"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "Juego publicado exitosamente",
                    content = @Content(schema = @Schema(implementation = Game.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "403",
                    description = "No autorizado como publisher",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response publishGame(@Valid PublishGameRequest request) {
        try {
            log.info("POST /api/publisher/games - Publishing game: {} by publisher: {}",
                    request.title, request.publisherId);

            PublisherUseCase.PublishGameCommand command = PublisherUseCase.PublishGameCommand.builder()
                    .title(request.title)
                    .description(request.description)
                    .shortDescription(request.shortDescription)
                    .price(request.price)
                    .currency(request.currency)
                    .developer(request.developer)
                    .publisherId(request.publisherId)
                    .releaseDate(request.releaseDate)
                    .categories(request.categories)
                    .tags(request.tags)
                    .images(request.images)
                    .systemRequirements(mapToSystemRequirementsDto(request.systemRequirements))
                    .stock(request.stock)
                    .isPreOrderAvailable(request.isPreOrderAvailable)
                    .build();

            Game game = publisherUseCase.publishGame(command);

            log.info("Game published successfully with ID: {} by publisher: {}",
                    game.getId(), request.publisherId);
            return Response.status(Response.Status.CREATED).entity(game).build();

        } catch (IllegalArgumentException e) {
            log.warn("Bad request for publishGame: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.builder()
                            .message("Error de validación")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (SecurityException e) {
            log.warn("Unauthorized publish attempt by: {}", request.publisherId);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ErrorResponse.builder()
                            .message("No autorizado")
                            .details("No tienes permisos para publicar juegos")
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in publishGame", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al publicar juego")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/{publisherId}/games")
    @Operation(
            summary = "Obtener juegos del publisher",
            description = "Obtiene todos los juegos publicados por un publisher específico"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Juegos del publisher obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = Game.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Publisher no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response getPublisherGames(
            @Parameter(description = "ID del publisher", required = true, example = "publisher123")
            @PathParam("publisherId") String publisherId,

            @Parameter(description = "Número de página", example = "0")
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,

            @Parameter(description = "Tamaño de página", example = "20")
            @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size) {
        try {
            log.info("GET /api/publisher/{}/games - page: {}, size: {}", publisherId, page, size);

            List<Game> games = publisherUseCase.getPublisherGames(publisherId, page, size);

            log.info("Found {} games for publisher: {}", games.size(), publisherId);
            return Response.ok(games).build();

        } catch (IllegalArgumentException e) {
            log.warn("Publisher not found: {}", publisherId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Publisher no encontrado")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in getPublisherGames for publisher: " + publisherId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener juegos del publisher")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/{publisherId}/statistics")
    @Operation(
            summary = "Obtener estadísticas del publisher",
            description = "Obtiene estadísticas consolidadas de todos los juegos del publisher"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Estadísticas obtenidas exitosamente",
                    content = @Content(schema = @Schema(implementation = SalesStatistics.class))
            ),
            @APIResponse(
                    responseCode = "403",
                    description = "No autorizado para ver estas estadísticas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Publisher no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response getPublisherStatistics(
            @Parameter(description = "ID del publisher", required = true)
            @PathParam("publisherId") String publisherId) {
        try {
            log.info("GET /api/publisher/{}/statistics", publisherId);

            List<SalesStatistics> statistics = publisherUseCase.getPublisherStatistics(publisherId);

            log.info("Retrieved statistics for {} games from publisher: {}", statistics.size(), publisherId);
            return Response.ok(statistics).build();

        } catch (IllegalArgumentException e) {
            log.warn("Publisher not found for statistics: {}", publisherId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Publisher no encontrado")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (SecurityException e) {
            log.warn("Unauthorized statistics access attempt for publisher: {}", publisherId);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ErrorResponse.builder()
                            .message("No autorizado")
                            .details("No tienes permisos para ver estas estadísticas")
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in getPublisherStatistics for publisher: " + publisherId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener estadísticas del publisher")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/{publisherId}/games/{gameId}/statistics")
    @Operation(
            summary = "Obtener estadísticas de juego específico",
            description = "Obtiene estadísticas detalladas de un juego específico del publisher"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Estadísticas del juego obtenidas exitosamente",
                    content = @Content(schema = @Schema(implementation = SalesStatistics.class))
            ),
            @APIResponse(
                    responseCode = "403",
                    description = "No autorizado para ver estas estadísticas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Juego o publisher no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response getGameSalesStatistics(
            @Parameter(description = "ID del publisher", required = true)
            @PathParam("publisherId") String publisherId,

            @Parameter(description = "ID del juego", required = true)
            @PathParam("gameId") String gameId) {
        try {
            log.info("GET /api/publisher/{}/games/{}/statistics", publisherId, gameId);

            SalesStatistics statistics = publisherUseCase.getGameSalesStatistics(gameId, publisherId);

            log.info("Retrieved statistics for game: {} from publisher: {}", gameId, publisherId);
            return Response.ok(statistics).build();

        } catch (IllegalArgumentException e) {
            log.warn("Game or publisher not found - gameId: {}, publisherId: {}", gameId, publisherId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Recurso no encontrado")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (SecurityException e) {
            log.warn("Unauthorized game statistics access - gameId: {}, publisherId: {}", gameId, publisherId);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ErrorResponse.builder()
                            .message("No autorizado")
                            .details("No tienes permisos para ver estas estadísticas")
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in getGameSalesStatistics - gameId: {}, publisherId: {}",
                    gameId, publisherId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener estadísticas del juego")
                            .build())
                    .build();
        }
    }

    @PUT
    @Path("/{publisherId}/games/{gameId}/price")
    @Operation(
            summary = "Actualizar precio del juego",
            description = "Actualiza el precio de un juego específico del publisher"
    )
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Precio actualizado exitosamente"),
            @APIResponse(
                    responseCode = "400",
                    description = "Datos de precio inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "403",
                    description = "No autorizado para actualizar este juego",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
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
    public Response updateGamePrice(
            @Parameter(description = "ID del publisher", required = true)
            @PathParam("publisherId") String publisherId,

            @Parameter(description = "ID del juego", required = true)
            @PathParam("gameId") String gameId,

            @Valid UpdatePriceRequest request) {
        try {
            log.info("PUT /api/publisher/{}/games/{}/price - New price: {} {}",
                    publisherId, gameId, request.price, request.currency);

            publisherUseCase.updateGamePrice(gameId, publisherId, request.price, request.currency);

            log.info("Price updated successfully for game: {} by publisher: {}", gameId, publisherId);
            return Response.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("Bad request for updateGamePrice: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.builder()
                            .message("Datos de precio inválidos")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (SecurityException e) {
            log.warn("Unauthorized price update attempt - gameId: {}, publisherId: {}", gameId, publisherId);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ErrorResponse.builder()
                            .message("No autorizado")
                            .details("No tienes permisos para actualizar este juego")
                            .build())
                    .build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            log.warn("Game not found for price update: {}", gameId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Juego no encontrado")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in updateGamePrice - gameId: {}, publisherId: {}",
                    gameId, publisherId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al actualizar precio del juego")
                            .build())
                    .build();
        }
    }

    // Helper methods
    private PublisherUseCase.SystemRequirementsDto mapToSystemRequirementsDto(SystemRequirementsRequest req) {
        if (req == null) return null;
        return PublisherUseCase.SystemRequirementsDto.builder()
                .minimumOS(req.minimumOS)
                .minimumProcessor(req.minimumProcessor)
                .minimumMemory(req.minimumMemory)
                .minimumGraphics(req.minimumGraphics)
                .minimumStorage(req.minimumStorage)
                .recommendedOS(req.recommendedOS)
                .recommendedProcessor(req.recommendedProcessor)
                .recommendedMemory(req.recommendedMemory)
                .recommendedGraphics(req.recommendedGraphics)
                .recommendedStorage(req.recommendedStorage)
                .build();
    }

    // Request DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Datos para publicar un nuevo juego")
    public static class PublishGameRequest {
        @NotBlank(message = "El título es obligatorio")
        @Size(min = 1, max = 255, message = "El título debe tener entre 1 y 255 caracteres")
        @Schema(description = "Título del juego", example = "Cyberpunk 2077", required = true)
        public String title;

        @NotBlank(message = "La descripción es obligatoria")
        @Size(min = 10, max = 5000, message = "La descripción debe tener entre 10 y 5000 caracteres")
        @Schema(description = "Descripción completa del juego", required = true)
        public String description;

        @Size(max = 500, message = "La descripción corta debe tener máximo 500 caracteres")
        @Schema(description = "Descripción corta del juego")
        public String shortDescription;

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
        @Schema(description = "Precio del juego", example = "59.99", required = true)
        public Double price;

        @NotBlank(message = "La moneda es obligatoria")
        @Pattern(regexp = "^[A-Z]{3}$", message = "La moneda debe ser un código de 3 letras")
        @Schema(description = "Código de moneda ISO", example = "USD", required = true)
        public String currency;

        @NotBlank(message = "El desarrollador es obligatorio")
        @Schema(description = "Nombre del desarrollador", example = "CD Projekt RED", required = true)
        public String developer;

        @NotBlank(message = "El ID del publisher es obligatorio")
        @Schema(description = "ID del publisher", example = "publisher123", required = true)
        public String publisherId;

        @NotBlank(message = "La fecha de lanzamiento es obligatoria")
        @Schema(description = "Fecha de lanzamiento en formato ISO", example = "2025-12-10T00:00:00", required = true)
        public String releaseDate;

        @Schema(description = "Lista de categorías del juego", example = "[\"RPG\", \"Accion\"]")
        public List<String> categories;

        @Schema(description = "Lista de tags del juego", example = "[\"futuristic\", \"open-world\"]")
        public List<String> tags;

        @Schema(description = "Lista de URLs de imágenes del juego")
        public List<String> images;

        @Schema(description = "Requisitos del sistema")
        public SystemRequirementsRequest systemRequirements;

        @Min(value = 0, message = "El stock no puede ser negativo")
        @Schema(description = "Stock disponible", example = "1000")
        public Integer stock;

        @Schema(description = "Si está disponible para pre-orden", example = "true")
        public Boolean isPreOrderAvailable;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Datos para actualizar precio de juego")
    public static class UpdatePriceRequest {
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
        @Schema(description = "Nuevo precio del juego", example = "49.99", required = true)
        public Double price;

        @NotBlank(message = "La moneda es obligatoria")
        @Pattern(regexp = "^[A-Z]{3}$", message = "La moneda debe ser un código de 3 letras")
        @Schema(description = "Código de moneda ISO", example = "USD", required = true)
        public String currency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Requisitos del sistema")
    public static class SystemRequirementsRequest {
        @Schema(description = "Sistema operativo mínimo", example = "Windows 10 64-bit")
        public String minimumOS;
        @Schema(description = "Procesador mínimo", example = "Intel Core i5-3570K")
        public String minimumProcessor;
        @Schema(description = "Memoria RAM mínima", example = "8 GB")
        public String minimumMemory;
        @Schema(description = "Tarjeta gráfica mínima", example = "NVIDIA GTX 780")
        public String minimumGraphics;
        @Schema(description = "Almacenamiento mínimo", example = "70 GB")
        public String minimumStorage;
        @Schema(description = "Sistema operativo recomendado", example = "Windows 11 64-bit")
        public String recommendedOS;
        @Schema(description = "Procesador recomendado", example = "Intel Core i7-4790")
        public String recommendedProcessor;
        @Schema(description = "Memoria RAM recomendada", example = "16 GB")
        public String recommendedMemory;
        @Schema(description = "Tarjeta gráfica recomendada", example = "NVIDIA RTX 2060")
        public String recommendedGraphics;
        @Schema(description = "Almacenamiento recomendado", example = "70 GB SSD")
        public String recommendedStorage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Respuesta de error estándar")
    public static class ErrorResponse {
        @Schema(description = "Mensaje de error", example = "Publisher no encontrado")
        public String message;
        @Schema(description = "Detalles adicionales del error")
        public String details;
        @Schema(description = "Timestamp del error", example = "2025-06-06T10:30:00Z")
        @Builder.Default
        public String timestamp = Instant.now().toString();
    }
}