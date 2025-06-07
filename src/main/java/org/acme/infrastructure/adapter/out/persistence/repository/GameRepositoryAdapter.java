package org.acme.infrastructure.adapter.out.persistence.repository;

import lombok.extern.slf4j.Slf4j;
import org.acme.domain.model.Game;
import org.acme.domain.model.valueobjects.*;
import org.acme.domain.repository.GameRepository;
import org.acme.infrastructure.adapter.out.persistence.entity.GameEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class GameRepositoryAdapter implements GameRepository, PanacheRepositoryBase<GameEntity, String> {

    @Override
    public Game save(Game game) {
        log.debug("Saving game: {}", game.getId());

        GameEntity entity = toEntity(game);
        persist(entity);

        log.debug("Game saved successfully: {}", entity.getId());
        return toDomain(entity);
    }

    @Override
    public Optional<Game> findById(GameId id) {
        log.debug("Finding game by ID: {}", id.getValue());

        Optional<GameEntity> entity = findByIdOptional(id.getValue());
        return entity.map(this::toDomain);
    }

    @Override
    public List<Game> findAll(int page, int size) {
        log.debug("Finding all games - page: {}, size: {}", page, size);

        return findAll()
                .page(Page.of(page, size))
                .list()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Game> findByCategory(Category category, int page, int size) {
        log.debug("Finding games by category: {} - page: {}, size: {}", category.getName(), page, size);

        return find("SELECT g FROM GameEntity g JOIN g.categories c WHERE c = ?1 AND g.isActive = true",
                category.getName())
                .page(Page.of(page, size))
                .list()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Game> findByPublisher(String publisherId, int page, int size) {
        log.debug("Finding games by publisher: {} - page: {}, size: {}", publisherId, page, size);

        return find("publisher = ?1 AND isActive = true", publisherId)
                .page(Page.of(page, size))
                .list()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Game> findAvailableForPreOrder() {
        log.debug("Finding games available for pre-order");

        return find("isPreOrderAvailable = true AND isActive = true AND releaseDate > ?1",
                LocalDateTime.now())
                .list()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Game> searchByTitle(String title, int page, int size) {
        log.debug("Searching games by title: '{}' - page: {}, size: {}", title, page, size);

        return find("LOWER(title) LIKE LOWER(?1) AND isActive = true", "%" + title + "%")
                .page(Page.of(page, size))
                .list()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Game> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, String currency) {
        log.debug("Finding games by price range: {} - {} {}", minPrice, maxPrice, currency);

        return find("price BETWEEN ?1 AND ?2 AND currency = ?3 AND isActive = true",
                minPrice, maxPrice, currency)
                .list()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Game> findByReleaseYear(int year) {
        log.debug("Finding games by release year: {}", year);

        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        return find("releaseDate BETWEEN ?1 AND ?2 AND isActive = true", startOfYear, endOfYear)
                .list()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Game> findTopRated(int limit) {
        log.debug("Finding top rated games - limit: {}", limit);

        return find("isActive = true AND rating.value IS NOT NULL ORDER BY rating.value DESC, rating.totalVotes DESC")
                .page(Page.ofSize(limit))
                .list()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Game> findRecentlyAdded(int limit) {
        log.debug("Finding recently added games - limit: {}", limit);

        return find("isActive = true ORDER BY createdAt DESC")
                .page(Page.ofSize(limit))
                .list()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Game> findDiscounted() {
        log.debug("Finding discounted games");

        // This is a simplified implementation
        // In a real scenario, you'd join with offers or have a discount field
        return find("isActive = true")
                .list()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(GameId id) {
        log.debug("Deleting game: {}", id.getValue());

        deleteById(id.getValue());

        log.debug("Game deleted successfully: {}", id.getValue());
    }

    @Override
    public long count() {
        return count("isActive = true");
    }

    @Override
    public long countByCategory(Category category) {
        return count("SELECT COUNT(g) FROM GameEntity g JOIN g.categories c WHERE c = ?1 AND g.isActive = true",
                category.getName());
    }

    @Override
    public boolean existsById(GameId id) {
        return findByIdOptional(id.getValue()).isPresent();
    }

    @Override
    public boolean existsByTitle(String title) {
        return count("title = ?1 AND isActive = true", title) > 0;
    }

    // Mapping methods
    private GameEntity toEntity(Game game) {
        GameEntity.SystemRequirementsEmbeddable sysReqEmbeddable = null;
        if (game.getSystemRequirements() != null) {
            SystemRequirements sysReq = game.getSystemRequirements();
            sysReqEmbeddable = GameEntity.SystemRequirementsEmbeddable.builder()
                    .minimumOS(sysReq.getMinimumOS())
                    .minimumProcessor(sysReq.getMinimumProcessor())
                    .minimumMemory(sysReq.getMinimumMemory())
                    .minimumGraphics(sysReq.getMinimumGraphics())
                    .minimumStorage(sysReq.getMinimumStorage())
                    .recommendedOS(sysReq.getRecommendedOS())
                    .recommendedProcessor(sysReq.getRecommendedProcessor())
                    .recommendedMemory(sysReq.getRecommendedMemory())
                    .recommendedGraphics(sysReq.getRecommendedGraphics())
                    .recommendedStorage(sysReq.getRecommendedStorage())
                    .build();
        }

        GameEntity.RatingEmbeddable ratingEmbeddable = null;
        if (game.getRating() != null) {
            Rating rating = game.getRating();
            ratingEmbeddable = GameEntity.RatingEmbeddable.builder()
                    .value(rating.getValue())
                    .totalVotes(rating.getTotalVotes())
                    .build();
        }

        Set<String> categoryNames = null;
        if (game.getCategories() != null) {
            categoryNames = game.getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.toSet());
        }

        return GameEntity.builder()
                .id(game.getId().getValue())
                .title(game.getTitle())
                .description(game.getDescription())
                .shortDescription(game.getShortDescription())
                .price(game.getPrice().getAmount())
                .currency(game.getPrice().getCurrency())
                .developer(game.getDeveloper())
                .publisher(game.getPublisher())
                .releaseDate(game.getReleaseDate())
                .categories(categoryNames)
                .tags(game.getTags())
                .images(game.getImages())
                .systemRequirements(sysReqEmbeddable)
                .rating(ratingEmbeddable)
                .stock(game.getStock())
                .isActive(game.getIsActive())
                .isPreOrderAvailable(game.getIsPreOrderAvailable())
                .createdAt(game.getCreatedAt())
                .updatedAt(game.getUpdatedAt())
                .build();
    }

    private Game toDomain(GameEntity entity) {
        GameId gameId = new GameId(entity.getId());
        Price price = new Price(entity.getPrice(), entity.getCurrency());

        SystemRequirements systemRequirements = null;
        if (entity.getSystemRequirements() != null) {
            GameEntity.SystemRequirementsEmbeddable sysReq = entity.getSystemRequirements();
            systemRequirements = SystemRequirements.builder()
                    .minimumOS(sysReq.getMinimumOS())
                    .minimumProcessor(sysReq.getMinimumProcessor())
                    .minimumMemory(sysReq.getMinimumMemory())
                    .minimumGraphics(sysReq.getMinimumGraphics())
                    .minimumStorage(sysReq.getMinimumStorage())
                    .recommendedOS(sysReq.getRecommendedOS())
                    .recommendedProcessor(sysReq.getRecommendedProcessor())
                    .recommendedMemory(sysReq.getRecommendedMemory())
                    .recommendedGraphics(sysReq.getRecommendedGraphics())
                    .recommendedStorage(sysReq.getRecommendedStorage())
                    .build();
        }

        Rating rating = null;
        if (entity.getRating() != null && entity.getRating().getValue() != null) {
            GameEntity.RatingEmbeddable ratingEmb = entity.getRating();
            rating = new Rating(ratingEmb.getValue(), ratingEmb.getTotalVotes());
        }

        Set<Category> categories = null;
        if (entity.getCategories() != null) {
            categories = entity.getCategories().stream()
                    .map(name -> new Category(name, ""))
                    .collect(Collectors.toSet());
        }

        return Game.builder()
                .id(gameId)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .shortDescription(entity.getShortDescription())
                .price(price)
                .developer(entity.getDeveloper())
                .publisher(entity.getPublisher())
                .releaseDate(entity.getReleaseDate())
                .categories(categories)
                .tags(entity.getTags())
                .images(entity.getImages())
                .systemRequirements(systemRequirements)
                .rating(rating)
                .stock(entity.getStock())
                .isActive(entity.getIsActive())
                .isPreOrderAvailable(entity.getIsPreOrderAvailable())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}