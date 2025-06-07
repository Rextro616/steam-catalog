package org.acme.domain.repository;

import org.acme.domain.model.Review;
import org.acme.domain.model.valueobjects.GameId;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository {
    Review save(Review review);
    Optional<Review> findById(String id);
    List<Review> findByGameId(GameId gameId, int page, int size);
    List<Review> findByUserId(String userId, int page, int size);
    List<Review> findPositiveByGameId(GameId gameId, int page, int size);
    List<Review> findNegativeByGameId(GameId gameId, int page, int size);
    List<Review> findMostHelpful(GameId gameId, int limit);
    List<Review> findRecent(GameId gameId, int limit);
    void delete(String id);
    long countByGameId(GameId gameId);
    long countPositiveByGameId(GameId gameId);
    long countNegativeByGameId(GameId gameId);
    boolean existsByUserAndGame(String userId, GameId gameId);
}

