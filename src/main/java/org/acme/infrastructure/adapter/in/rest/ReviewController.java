package org.acme.infrastructure.adapter.in.rest;

import org.acme.application.port.in.ReviewUseCase;
import org.acme.domain.model.Review;
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

@Path("/api/reviews")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Reviews", description = "Sistema de reseñas y calificaciones de juegos")
@Slf4j
public class ReviewController {

    @Inject
    ReviewUseCase reviewUseCase;

    @POST
    @Operation(
            summary = "Crear reseña",
            description = "Crea una nueva reseña para un juego"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "Reseña creada exitosamente",
                    content = @Content(schema = @Schema(implementation = Review.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Juego no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "409",
                    description = "El usuario ya tiene una reseña para este juego",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response createReview(@Valid CreateReviewRequest request) {
        try {
            log.info("POST /api/reviews - Creating review for game: {} by user: {}",
                    request.gameId, request.userId);

            ReviewUseCase.CreateReviewCommand command = ReviewUseCase.CreateReviewCommand.builder()
                    .gameId(request.gameId)
                    .userId(request.userId)
                    .content(request.content)
                    .isRecommended(request.isRecommended)
                    .build();

            Review review = reviewUseCase.createReview(command);

            log.info("Review created successfully with ID: {}", review.getId());
            return Response.status(Response.Status.CREATED).entity(review).build();

        } catch (IllegalArgumentException e) {
            log.warn("Bad request for createReview: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.builder()
                            .message("Error de validación")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            log.warn("Game not found for review: {}", request.gameId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Juego no encontrado")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (IllegalStateException e) {
            log.warn("Duplicate review attempt - gameId: {}, userId: {}", request.gameId, request.userId);
            return Response.status(Response.Status.CONFLICT)
                    .entity(ErrorResponse.builder()
                            .message("Reseña duplicada")
                            .details("Ya tienes una reseña para este juego")
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in createReview", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al crear reseña")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/game/{gameId}")
    @Operation(
            summary = "Obtener reseñas de un juego",
            description = "Obtiene todas las reseñas de un juego específico con paginación"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Reseñas obtenidas exitosamente",
                    content = @Content(schema = @Schema(implementation = Review.class))
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
    public Response getGameReviews(
            @Parameter(description = "ID del juego", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathParam("gameId") String gameId,

            @Parameter(description = "Número de página", example = "0")
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,

            @Parameter(description = "Tamaño de página", example = "20")
            @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size,

            @Parameter(description = "Filtrar por tipo", example = "positive",
                    schema = @Schema(enumeration = {"all", "positive", "negative", "helpful"}))
            @QueryParam("filter") @DefaultValue("all") String filter) {
        try {
            log.info("GET /api/reviews/game/{} - page: {}, size: {}, filter: {}",
                    gameId, page, size, filter);

            List<Review> reviews;
            switch (filter.toLowerCase()) {
                case "positive":
                    // Implementation would call a specific method for positive reviews
                    reviews = reviewUseCase.getGameReviews(gameId, page, size);
                    break;
                case "negative":
                    // Implementation would call a specific method for negative reviews
                    reviews = reviewUseCase.getGameReviews(gameId, page, size);
                    break;
                case "helpful":
                    reviews = reviewUseCase.getMostHelpfulReviews(gameId, size);
                    break;
                default:
                    reviews = reviewUseCase.getGameReviews(gameId, page, size);
                    break;
            }

            log.info("Found {} reviews for game: {} with filter: {}", reviews.size(), gameId, filter);
            return Response.ok(reviews).build();

        } catch (IllegalArgumentException e) {
            log.warn("Game not found: {}", gameId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Juego no encontrado")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in getGameReviews for gameId: " + gameId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener reseñas del juego")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/user/{userId}")
    @Operation(
            summary = "Obtener reseñas de un usuario",
            description = "Obtiene todas las reseñas escritas por un usuario específico"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Reseñas del usuario obtenidas exitosamente",
                    content = @Content(schema = @Schema(implementation = Review.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response getUserReviews(
            @Parameter(description = "ID del usuario", required = true, example = "user123")
            @PathParam("userId") String userId,

            @Parameter(description = "Número de página", example = "0")
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,

            @Parameter(description = "Tamaño de página", example = "20")
            @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size) {
        try {
            log.info("GET /api/reviews/user/{} - page: {}, size: {}", userId, page, size);

            List<Review> reviews = reviewUseCase.getUserReviews(userId, page, size);

            log.info("Found {} reviews for user: {}", reviews.size(), userId);
            return Response.ok(reviews).build();

        } catch (Exception e) {
            log.error("Internal error in getUserReviews for userId: " + userId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener reseñas del usuario")
                            .build())
                    .build();
        }
    }

    @POST
    @Path("/{reviewId}/vote")
    @Operation(
            summary = "Votar reseña",
            description = "Vota si una reseña es útil o no"
    )
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Voto registrado exitosamente"),
            @APIResponse(
                    responseCode = "400",
                    description = "Parámetro de voto inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Reseña no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response voteReview(
            @Parameter(description = "ID de la reseña", required = true)
            @PathParam("reviewId") String reviewId,

            @Parameter(description = "Si el voto es útil", required = true, example = "true")
            @QueryParam("helpful") @NotNull Boolean isHelpful) {
        try {
            log.info("POST /api/reviews/{}/vote - helpful: {}", reviewId, isHelpful);

            reviewUseCase.voteReview(reviewId, isHelpful);

            log.info("Vote registered for review: {} as {}", reviewId, isHelpful ? "helpful" : "not helpful");
            return Response.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("Review not found for voting: {}", reviewId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Reseña no encontrada")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in voteReview for reviewId: " + reviewId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al votar reseña")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/game/{gameId}/helpful")
    @Operation(
            summary = "Obtener reseñas más útiles",
            description = "Obtiene las reseñas más útiles de un juego específico"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Reseñas más útiles obtenidas exitosamente",
                    content = @Content(schema = @Schema(implementation = Review.class))
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
    public Response getMostHelpfulReviews(
            @Parameter(description = "ID del juego", required = true)
            @PathParam("gameId") String gameId,

            @Parameter(description = "Número máximo de reseñas", example = "10")
            @QueryParam("limit") @DefaultValue("10") @Min(1) @Max(50) int limit) {
        try {
            log.info("GET /api/reviews/game/{}/helpful - limit: {}", gameId, limit);

            List<Review> reviews = reviewUseCase.getMostHelpfulReviews(gameId, limit);

            log.info("Found {} most helpful reviews for game: {}", reviews.size(), gameId);
            return Response.ok(reviews).build();

        } catch (IllegalArgumentException e) {
            log.warn("Game not found: {}", gameId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Juego no encontrado")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in getMostHelpfulReviews for gameId: " + gameId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener reseñas más útiles")
                            .build())
                    .build();
        }
    }

    @DELETE
    @Path("/{reviewId}/user/{userId}")
    @Operation(
            summary = "Eliminar reseña",
            description = "Elimina una reseña del usuario"
    )
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Reseña eliminada exitosamente"),
            @APIResponse(
                    responseCode = "403",
                    description = "No autorizado para eliminar esta reseña",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Reseña no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response deleteReview(
            @Parameter(description = "ID de la reseña", required = true)
            @PathParam("reviewId") String reviewId,

            @Parameter(description = "ID del usuario", required = true)
            @PathParam("userId") String userId) {
        try {
            log.info("DELETE /api/reviews/{}/user/{}", reviewId, userId);

            reviewUseCase.deleteReview(reviewId, userId);

            log.info("Review deleted successfully: {} by user: {}", reviewId, userId);
            return Response.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("Review not found for deletion: {}", reviewId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Reseña no encontrada")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (SecurityException e) {
            log.warn("Unauthorized review deletion attempt - reviewId: {}, userId: {}", reviewId, userId);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ErrorResponse.builder()
                            .message("No autorizado")
                            .details("No puedes eliminar esta reseña")
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in deleteReview - reviewId: {}, userId: {}", reviewId, userId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al eliminar reseña")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/{reviewId}")
    @Operation(
            summary = "Obtener reseña por ID",
            description = "Obtiene los detalles de una reseña específica"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Reseña encontrada",
                    content = @Content(schema = @Schema(implementation = Review.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Reseña no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response getReviewById(
            @Parameter(description = "ID de la reseña", required = true)
            @PathParam("reviewId") String reviewId) {
        try {
            log.info("GET /api/reviews/{}", reviewId);

            // Note: This would require implementing getReviewById in the use case
            return Response.status(Response.Status.NOT_IMPLEMENTED)
                    .entity(ErrorResponse.builder()
                            .message("Funcionalidad no implementada")
                            .details("El método getReviewById aún no está implementado")
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("Internal error in getReviewById for id: " + reviewId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener reseña")
                            .build())
                    .build();
        }
    }

    // Request DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Datos para crear una reseña")
    public static class CreateReviewRequest {
        @NotBlank(message = "El ID del juego es obligatorio")
        @Schema(description = "ID del juego a reseñar", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
        public String gameId;

        @NotBlank(message = "El ID del usuario es obligatorio")
        @Schema(description = "ID del usuario que escribe la reseña", example = "user123", required = true)
        public String userId;

        @NotBlank(message = "El contenido de la reseña es obligatorio")
        @Size(min = 10, max = 5000, message = "La reseña debe tener entre 10 y 5000 caracteres")
        @Schema(description = "Contenido de la reseña",
                example = "Excelente juego con gráficos impresionantes y una historia cautivadora...",
                required = true)
        public String content;

        @NotNull(message = "La recomendación es obligatoria")
        @Schema(description = "Si recomienda el juego", example = "true", required = true)
        public Boolean isRecommended;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Respuesta de error estándar")
    public static class ErrorResponse {
        @Schema(description = "Mensaje de error", example = "Reseña no encontrada")
        public String message;

        @Schema(description = "Detalles adicionales del error", example = "No existe una reseña con el ID especificado")
        public String details;

        @Schema(description = "Timestamp del error", example = "2025-06-06T10:30:00Z")
        @Builder.Default
        public String timestamp = Instant.now().toString();
    }
}