package org.acme.domain.repository;

import org.acme.domain.model.Bundle;
import org.acme.domain.model.valueobjects.GameId;
import java.util.List;
import java.util.Optional;

public interface BundleRepository {
    Bundle save(Bundle bundle);
    Optional<Bundle> findById(String id);
    List<Bundle> findAll(int page, int size);
    List<Bundle> findActiveBundles();
    List<Bundle> findByGameId(GameId gameId);
    List<Bundle> findExpiringSoon(int hours);
    void delete(String id);
    long count();
    boolean existsById(String id);
}