package org.acme.application.service;

import org.acme.application.port.in.GameUseCase;
import org.acme.application.port.out.AnalyticsServicePort;
import org.acme.application.port.out.NotificationServicePort;
import org.acme.domain.model.Game;
import org.acme.domain.model.valueobjects.*;
import org.acme.domain.repository.GameRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
@Slf4j
public class GameApplicationService implements GameUseCase {

    @Inject
    GameRepository gameRepository;

    @Inject
    AnalyticsServicePort analyticsService;

    @Inject
    NotificationServicePort notificationService;

    @Override
    public Game createGame(CreateGameCommand command) {
        log.info("Creating new game: {}", command.title);

        validateCreateGameCommand(command);

        GameId gameId = GameId.generate();
        Price price = new Price(BigDecimal.valueOf(command.price), command.currency);
        LocalDateTime releaseDate = LocalDateTime.parse(command.releaseDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Game game = Game.builder()
                .id(gameId)
                .title(command.title)
                .description(command.description)
                .shortDescription(command.shortDescription)
                .price(price)
                .developer(command.developer)
                .publisher(command.publisher)
                .releaseDate(releaseDate)
                .stock(command.stock)
                .isPreOrderAvailable(command.isPreOrderAvailable)
                .build();

        if (command.categories != null && !command.categories.isEmpty()) {
            Set<Category> categories = command.categories.stream()
                    .map(cat -> new Category(cat, ""))
                    .collect(Collectors.toSet());
            game.updateCategories(categories);
        }

        if (command.images != null) {
            game.updateImages(command.images);
        }

        if (command.systemRequirements != null) {
            SystemRequirements sysReq = mapToSystemRequirements(command.systemRequirements);
            game.updateSystemRequirements(sysReq);
        }

        if (command.tags != null) {
            game.setTags(Set.copyOf(command.tags));
        }

        Game savedGame = gameRepository.save(game);

        log.info("Game created successfully with ID: {}", savedGame.getId());
        return savedGame;
    }

    @Override
    public Game getGameById(GameId id) {
        log.debug("Fetching game by ID: {}", id);

        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado: " + id));

        analyticsService.trackGameView(AnalyticsServicePort.GameViewEvent.builder()
                .gameId(id.getValue())
                .source("direct")
                .timestamp(LocalDateTime.now())
                .build());

        return game;
    }

    @Override
    public List<Game> getAllGames(int page, int size) {
        log.debug("Fetching all games - page: {}, size: {}", page, size);
        validatePagination(page, size);
        return gameRepository.findAll(page, size);
    }

    @Override
    public List<Game> getGamesByCategory(Category category, int page, int size) {
        log.debug("Fetching games by category: {} - page: {}, size: {}", category.getName(), page, size);
        validatePagination(page, size);
        return gameRepository.findByCategory(category, page, size);
    }

    @Override
    public Game updateGame(UpdateGameCommand command) {
        log.info("Updating game: {}", command.id);

        GameId gameId = new GameId(command.id);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado: " + command.id));

        if (command.title != null && !command.title.trim().isEmpty()) {
            game.setTitle(command.title);
        }

        if (command.description != null) {
            game.setDescription(command.description);
        }

        if (command.shortDescription != null) {
            game.setShortDescription(command.shortDescription);
        }

        if (command.stock != null) {
            game.updateStock(command.stock);
        }

        if (command.price != null && command.currency != null) {
            Price newPrice = new Price(BigDecimal.valueOf(command.price), command.currency);
            game.setPrice(newPrice);
        }

        if (command.categories != null) {
            Set<Category> categories = command.categories.stream()
                    .map(cat -> new Category(cat, ""))
                    .collect(Collectors.toSet());
            game.updateCategories(categories);
        }

        if (command.images != null) {
            game.updateImages(command.images);
        }

        if (command.systemRequirements != null) {
            SystemRequirements sysReq = mapToSystemRequirements(command.systemRequirements);
            game.updateSystemRequirements(sysReq);
        }

        if (command.isPreOrderAvailable != null) {
            game.updatePreOrderAvailability(command.isPreOrderAvailable);
        }

        game.setUpdatedAt(LocalDateTime.now());

        Game updatedGame = gameRepository.save(game);
        log.info("Game updated successfully: {}", updatedGame.getId());

        return updatedGame;
    }

    @Override
    public void deleteGame(GameId id) {
        log.info("Deleting game: {}", id);

        if (!gameRepository.existsById(id)) {
            throw new IllegalArgumentException("Juego no encontrado: " + id);
        }

        gameRepository.delete(id);
        log.info("Game deleted successfully: {}", id);
    }

    @Override
    public List<Game> searchGames(String title, int page, int size) {
        log.debug("Searching games by title: '{}' - page: {}, size: {}", title, page, size);

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("El título de búsqueda no puede estar vacío");
        }

        validatePagination(page, size);

        List<Game> results = gameRepository.searchByTitle(title.trim(), page, size);

        analyticsService.trackSearchQuery(AnalyticsServicePort.SearchQueryEvent.builder()
                .query(title)
                .resultsCount(results.size())
                .timestamp(LocalDateTime.now())
                .build());

        return results;
    }

    @Override
    public List<Game> getTopRatedGames(int limit) {
        log.debug("Fetching top rated games - limit: {}", limit);

        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("El límite debe estar entre 1 y 100");
        }

        return gameRepository.findTopRated(limit);
    }

    @Override
    public List<Game> getRecentlyAddedGames(int limit) {
        log.debug("Fetching recently added games - limit: {}", limit);

        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("El límite debe estar entre 1 y 100");
        }

        return gameRepository.findRecentlyAdded(limit);
    }

    @Override
    public List<Game> getDiscountedGames() {
        log.debug("Fetching discounted games");
        return gameRepository.findDiscounted();
    }

    private void validateCreateGameCommand(CreateGameCommand command) {
        if (command.title == null || command.title.trim().isEmpty()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }

        if (command.price == null || command.price < 0) {
            throw new IllegalArgumentException("El precio debe ser mayor o igual a 0");
        }

        if (command.currency == null || command.currency.trim().isEmpty()) {
            throw new IllegalArgumentException("La moneda es obligatoria");
        }

        if (command.developer == null || command.developer.trim().isEmpty()) {
            throw new IllegalArgumentException("El desarrollador es obligatorio");
        }

        if (command.publisher == null || command.publisher.trim().isEmpty()) {
            throw new IllegalArgumentException("El publisher es obligatorio");
        }

        if (gameRepository.existsByTitle(command.title)) {
            throw new IllegalArgumentException("Ya existe un juego con ese título");
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("La página no puede ser negativa");
        }

        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("El tamaño de página debe estar entre 1 y 100");
        }
    }

    private SystemRequirements mapToSystemRequirements(SystemRequirementsDto dto) {
        return SystemRequirements.builder()
                .minimumOS(dto.minimumOS)
                .minimumProcessor(dto.minimumProcessor)
                .minimumMemory(dto.minimumMemory)
                .minimumGraphics(dto.minimumGraphics)
                .minimumStorage(dto.minimumStorage)
                .recommendedOS(dto.recommendedOS)
                .recommendedProcessor(dto.recommendedProcessor)
                .recommendedMemory(dto.recommendedMemory)
                .recommendedGraphics(dto.recommendedGraphics)
                .recommendedStorage(dto.recommendedStorage)
                .build();
    }
}