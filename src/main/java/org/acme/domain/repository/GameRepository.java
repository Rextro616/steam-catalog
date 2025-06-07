package org.acme.domain.repository;

import org.acme.domain.model.Game;
import org.acme.domain.model.valueobjects.GameId;
import org.acme.domain.model.valueobjects.Category;
import java.util.List;
import java.util.Optional;

public interface GameRepository {
    Game save(Game game);
    Optional<Game> findById(GameId id);
    List<Game> findAll(int page, int size);
    List<Game> findByCategory(Category category, int page, int size);
    List<Game> findByPublisher(String publisherId, int page, int size);
    List<Game> findAvailableForPreOrder();
    List<Game> searchByTitle(String title, int page, int size);
    List<Game> findByPriceRange(java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, String currency);
    List<Game> findByReleaseYear(int year);
    List<Game> findTopRated(int limit);
    List<Game> findRecentlyAdded(int limit);
    List<Game> findDiscounted();
    void delete(GameId id);
    long count();
    long countByCategory(Category category);
    boolean existsById(GameId id);
    boolean existsByTitle(String title);
}