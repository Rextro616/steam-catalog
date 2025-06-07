package org.acme.domain.repository;

import org.acme.domain.model.DLC;
import org.acme.domain.model.valueobjects.GameId;
import java.util.List;
import java.util.Optional;

public interface DLCRepository {
    DLC save(DLC dlc);
    Optional<DLC> findById(String id);
    List<DLC> findByBaseGameId(GameId baseGameId);
    List<DLC> findByContentType(String contentType);
    List<DLC> findAvailable();
    void delete(String id);
    long count();
    long countByBaseGameId(GameId baseGameId);
    boolean existsById(String id);
}
