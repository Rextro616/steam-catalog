package org.acme.domain.repository;

import org.acme.domain.model.PreOrder;
import org.acme.domain.model.valueobjects.GameId;

import java.util.List;
import java.util.Optional;

public interface PreOrderRepository {
    PreOrder save(PreOrder preOrder);
    Optional<PreOrder> findById(String id);
    List<PreOrder> findByUserId(String userId);
    List<PreOrder> findByGameId(GameId gameId);
    List<PreOrder> findByStatus(String status);
    List<PreOrder> findReadyToComplete();
    void delete(String id);
    long count();
    long countByGameId(GameId gameId);
    boolean existsByUserAndGame(String userId, GameId gameId);
}