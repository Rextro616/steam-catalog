package org.acme.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.acme.application.port.in.GameUseCase;
import org.acme.domain.model.Game;
import org.acme.domain.model.valueobjects.GameId;
import org.acme.domain.model.valueobjects.Category;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.util.List;

@Path("/api/games")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Games", description = "Operaciones relacionadas con el catálogo de juegos")
@Slf4j
public class GameController {

    @Inject
    GameUseCase gameUseCase;

    @GET
    @Operation(
            summary = "Obtener lista de juegos",
            description = "Obtiene una lista paginada de todos los juegos disponibles en el catálogo, " +
                    "con opción de filtrar por categoría"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Lista de juegos obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = Game.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Parámetros de consulta inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response getAllGames(
            @Parameter(description = "Número de página (comienza en 0)", example = "0")
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,

            @Parameter(description = "Tamaño de página", example = "20")
            @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size,

            @Parameter(description = "Filtrar por categoría", example = "Accion")
            @QueryParam("category") String category) {

        try {
            log.info("GET /api/games - page: {}, size: {}, category: {}", page, size, category);

            List<Game> games;
            if (category != null && !category.isEmpty()) {
                Category cat = new Category(category, "");
                games = gameUseCase.getGamesByCategory(cat, page, size);
            } else {
                games = gameUseCase.getAllGames(page, size);
            }

            log.info("Found {} games", games.size());
            return Response.ok(games).build();

        } catch (IllegalArgumentException e) {
            log.warn("Bad request for getAllGames: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.builder()
                            .message("Parámetros inválidos")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in getAllGames", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener juegos")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Operation(
            summary = "Obtener juego por ID",
            description = "Obtiene la información completa de un juego específico incluyendo " +
                    "título, descripción, precio, imágenes, requisitos del sistema y stock"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Juego encontrado",
                    content = @Content(schema = @Schema(implementation = Game.class))
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
    public Response getGameById(
            @Parameter(
                    description = "ID único del juego",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathParam("id") String id) {

        try {
            log.info("GET /api/games/{}", id);

            GameId gameId = new GameId(id);
            Game game = gameUseCase.getGameById(gameId);

            log.info("Game found: {}", game.getTitle());
            return Response.ok(game).build();

        } catch (IllegalArgumentException e) {
            log.warn("Game not found: {}", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Juego no encontrado")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in getGameById for id: " + id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener juego")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/search")
    @Operation(
            summary = "Buscar juegos por título",
            description = "Busca juegos que coincidan con el título especificado"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Búsqueda realizada exitosamente",
                    content = @Content(schema = @Schema(implementation = Game.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Parámetros de búsqueda inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Response searchGames(
            @Parameter(description = "Título del juego a buscar", required = true, example = "Cyberpunk")
            @QueryParam("title") @NotBlank String title,

            @Parameter(description = "Número de página", example = "0")
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,

            @Parameter(description = "Tamaño de página", example = "20")
            @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size) {

        try {
            log.info("GET /api/games/search - title: '{}', page: {}, size: {}", title, page, size);

            if (title == null || title.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.builder()
                                .message("Título de búsqueda requerido")
                                .details("El parámetro 'title' no puede estar vacío")
                                .build())
                        .build();
            }

            List<Game> games = gameUseCase.searchGames(title, page, size);

            log.info("Search completed. Found {} games for title: '{}'", games.size(), title);
            return Response.ok(games).build();

        } catch (IllegalArgumentException e) {
            log.warn("Bad request for searchGames: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.builder()
                            .message("Parámetros de búsqueda inválidos")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in searchGames for title: " + title, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error en la búsqueda")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/top-rated")
    @Operation(
            summary = "Obtener juegos mejor calificados",
            description = "Obtiene una lista de los juegos con mejores calificaciones"
    )
    public Response getTopRatedGames(
            @Parameter(description = "Número máximo de juegos a retornar", example = "10")
            @QueryParam("limit") @DefaultValue("10") @Min(1) @Max(50) int limit) {

        try {
            log.info("GET /api/games/top-rated - limit: {}", limit);

            List<Game> games = gameUseCase.getTopRatedGames(limit);

            log.info("Found {} top-rated games", games.size());
            return Response.ok(games).build();

        } catch (Exception e) {
            log.error("Internal error in getTopRatedGames", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener juegos mejor calificados")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/recent")
    @Operation(
            summary = "Obtener juegos agregados recientemente",
            description = "Obtiene una lista de los juegos agregados más recientemente al catálogo"
    )
    public Response getRecentlyAddedGames(
            @Parameter(description = "Número máximo de juegos a retornar", example = "10")
            @QueryParam("limit") @DefaultValue("10") @Min(1) @Max(50) int limit) {

        try {
            log.info("GET /api/games/recent - limit: {}", limit);

            List<Game> games = gameUseCase.getRecentlyAddedGames(limit);

            log.info("Found {} recently added games", games.size());
            return Response.ok(games).build();

        } catch (Exception e) {
            log.error("Internal error in getRecentlyAddedGames", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener juegos recientes")
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/discounted")
    @Operation(
            summary = "Obtener juegos con descuento",
            description = "Obtiene una lista de todos los juegos que actualmente tienen descuento"
    )
    public Response getDiscountedGames() {
        try {
            log.info("GET /api/games/discounted");

            List<Game> games = gameUseCase.getDiscountedGames();

            log.info("Found {} discounted games", games.size());
            return Response.ok(games).build();

        } catch (Exception e) {
            log.error("Internal error in getDiscountedGames", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al obtener juegos con descuento")
                            .build())
                    .build();
        }
    }

    @POST
    @Operation(
            summary = "Crear nuevo juego",
            description = "Crea un nuevo juego en el catálogo"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "Juego creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Game.class))
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
    public Response createGame(@Valid CreateGameRequest request) {
        try {
            log.info("POST /api/games - Creating game: {}", request.title);

            GameUseCase.CreateGameCommand command = GameUseCase.CreateGameCommand.builder()
                    .title(request.title)
                    .description(request.description)
                    .shortDescription(request.shortDescription)
                    .price(request.price)
                    .currency(request.currency)
                    .developer(request.developer)
                    .publisher(request.publisher)
                    .releaseDate(request.releaseDate)
                    .categories(request.categories)
                    .tags(request.tags)
                    .images(request.images)
                    .systemRequirements(mapToSystemRequirementsDto(request.systemRequirements))
                    .stock(request.stock)
                    .isPreOrderAvailable(request.isPreOrderAvailable)
                    .build();

            Game game = gameUseCase.createGame(command);

            log.info("Game created successfully with ID: {}", game.getId());
            return Response.status(Response.Status.CREATED).entity(game).build();

        } catch (IllegalArgumentException e) {
            log.warn("Bad request for createGame: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.builder()
                            .message("Error de validación")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in createGame", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al crear juego")
                            .build())
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(
            summary = "Actualizar juego",
            description = "Actualiza la información de un juego existente"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Juego actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = Game.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Juego no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
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
    public Response updateGame(
            @Parameter(description = "ID del juego a actualizar", required = true)
            @PathParam("id") String id,
            @Valid UpdateGameRequest request) {

        try {
            log.info("PUT /api/games/{} - Updating game", id);

            GameUseCase.UpdateGameCommand command = GameUseCase.UpdateGameCommand.builder()
                    .id(id)
                    .title(request.title)
                    .description(request.description)
                    .shortDescription(request.shortDescription)
                    .price(request.price)
                    .currency(request.currency)
                    .categories(request.categories)
                    .tags(request.tags)
                    .images(request.images)
                    .systemRequirements(mapToSystemRequirementsDto(request.systemRequirements))
                    .stock(request.stock)
                    .isPreOrderAvailable(request.isPreOrderAvailable)
                    .build();

            Game game = gameUseCase.updateGame(command);

            log.info("Game updated successfully: {}", id);
            return Response.ok(game).build();

        } catch (IllegalArgumentException e) {
            log.warn("Game not found for update: {}", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Juego no encontrado")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in updateGame for id: " + id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al actualizar juego")
                            .build())
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(
            summary = "Eliminar juego",
            description = "Elimina un juego del catálogo"
    )
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Juego eliminado exitosamente"),
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
    public Response deleteGame(
            @Parameter(description = "ID del juego a eliminar", required = true)
            @PathParam("id") String id) {

        try {
            log.info("DELETE /api/games/{}", id);

            GameId gameId = new GameId(id);
            gameUseCase.deleteGame(gameId);

            log.info("Game deleted successfully: {}", id);
            return Response.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("Game not found for deletion: {}", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .message("Juego no encontrado")
                            .details(e.getMessage())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Internal error in deleteGame for id: " + id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.builder()
                            .message("Error interno del servidor")
                            .details("Error al eliminar juego")
                            .build())
                    .build();
        }
    }

    // Helper methods
    private GameUseCase.SystemRequirementsDto mapToSystemRequirementsDto(SystemRequirementsRequest req) {
        if (req == null) return null;
        return GameUseCase.SystemRequirementsDto.builder()
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Datos para crear un nuevo juego")
    public static class CreateGameRequest {
        @NotBlank(message = "El título es obligatorio")
        @Size(min = 1, max = 255, message = "El título debe tener entre 1 y 255 caracteres")
        @Schema(description = "Título del juego", example = "Cyberpunk 2077", required = true)
        public String title;

        @NotBlank(message = "La descripción es obligatoria")
        @Size(min = 10, max = 5000, message = "La descripción debe tener entre 10 y 5000 caracteres")
        @Schema(description = "Descripción completa del juego", example = "Un juego de rol futurista...", required = true)
        public String description;

        @Size(max = 500, message = "La descripción corta debe tener máximo 500 caracteres")
        @Schema(description = "Descripción corta del juego", example = "RPG futurista de mundo abierto")
        public String shortDescription;

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
        @Schema(description = "Precio del juego", example = "59.99", required = true)
        public Double price;

        @NotBlank(message = "La moneda es obligatoria")
        @Pattern(regexp = "^[A-Z]{3}$", message = "La moneda debe ser un código de 3 letras (ej: USD)")
        @Schema(description = "Código de moneda ISO", example = "USD", required = true)
        public String currency;

        @NotBlank(message = "El desarrollador es obligatorio")
        @Size(min = 1, max = 255, message = "El desarrollador debe tener entre 1 y 255 caracteres")
        @Schema(description = "Nombre del desarrollador", example = "CD Projekt RED", required = true)
        public String developer;

        @NotBlank(message = "El publisher es obligatorio")
        @Size(min = 1, max = 255, message = "El publisher debe tener entre 1 y 255 caracteres")
        @Schema(description = "Nombre del publisher", example = "CD Projekt", required = true)
        public String publisher;

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
    @Schema(description = "Datos para actualizar un juego")
    public static class UpdateGameRequest {
        @Size(min = 1, max = 255, message = "El título debe tener entre 1 y 255 caracteres")
        @Schema(description = "Título del juego", example = "Cyberpunk 2077 Enhanced Edition")
        public String title;

        @Size(min = 10, max = 5000, message = "La descripción debe tener entre 10 y 5000 caracteres")
        @Schema(description = "Descripción completa del juego")
        public String description;

        @Size(max = 500, message = "La descripción corta debe tener máximo 500 caracteres")
        @Schema(description = "Descripción corta del juego")
        public String shortDescription;

        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
        @Schema(description = "Precio del juego", example = "49.99")
        public Double price;

        @Pattern(regexp = "^[A-Z]{3}$", message = "La moneda debe ser un código de 3 letras")
        @Schema(description = "Código de moneda ISO", example = "USD")
        public String currency;

        @Schema(description = "Lista de categorías del juego")
        public List<String> categories;

        @Schema(description = "Lista de tags del juego")
        public List<String> tags;

        @Schema(description = "Lista de URLs de imágenes del juego")
        public List<String> images;

        @Schema(description = "Requisitos del sistema")
        public SystemRequirementsRequest systemRequirements;

        @Min(value = 0, message = "El stock no puede ser negativo")
        @Schema(description = "Stock disponible", example = "500")
        public Integer stock;

        @Schema(description = "Si está disponible para pre-orden")
        public Boolean isPreOrderAvailable;
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
        @Schema(description = "Mensaje de error", example = "Juego no encontrado")
        public String message;

        @Schema(description = "Detalles adicionales del error", example = "No existe un juego con el ID especificado")
        public String details;

        @Schema(description = "Timestamp del error", example = "2025-06-06T10:30:00Z")
        @Builder.Default
        public String timestamp = Instant.now().toString();
    }
}