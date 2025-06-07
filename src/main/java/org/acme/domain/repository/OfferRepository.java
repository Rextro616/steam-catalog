package org.acme.domain.repository;

import org.acme.domain.model.Offer;
import org.acme.domain.model.valueobjects.GameId;
import java.util.List;
import java.util.Optional;

public interface OfferRepository {
    Offer save(Offer offer);
    Optional<Offer> findById(String id);
    List<Offer> findAll(int page, int size);
    List<Offer> findActiveOffers();
    List<Offer> findByOfferType(String offerType);
    List<Offer> findByGameId(GameId gameId);
    List<Offer> findExpiringSoon(int hours);
    List<Offer> findCurrentSeasonalOffers();
    void delete(String id);
    long count();
    boolean existsById(String id);
}
