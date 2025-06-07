package org.acme.domain.repository;

import org.acme.domain.model.SalesStatistics;
import org.acme.domain.model.valueobjects.GameId;
import java.util.List;
import java.util.Optional;

public interface SalesStatisticsRepository {
    SalesStatistics save(SalesStatistics statistics);
    Optional<SalesStatistics> findById(String id);
    Optional<SalesStatistics> findByGameId(GameId gameId);
    List<SalesStatistics> findByPublisherId(String publisherId);
    List<SalesStatistics> findTopSellingGames(int limit);
    List<SalesStatistics> findTopRevenueGames(int limit);
    void delete(String id);
    long count();
    boolean existsByGameId(GameId gameId);
}