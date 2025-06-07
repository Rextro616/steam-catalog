package org.acme.application.port.in;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acme.domain.model.Offer;

import java.util.List;

public interface OfferUseCase {
    List<Offer> getActiveOffers();
    List<Offer> getSeasonalOffers();
    Offer getOfferById(String id);
    List<Offer> getOffersByType(String offerType);
    List<Offer> getOffersByGame(String gameId);
    Offer createOffer(CreateOfferCommand command);
    void deactivateOffer(String offerId);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CreateOfferCommand {
        public String name;
        public String description;
        public List<String> gameIds;
        public Double discountPercentage;
        public String startDate;
        public String endDate;
        public String offerType;
    }
}
