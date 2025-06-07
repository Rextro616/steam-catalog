package org.acme.application.port.in;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acme.domain.model.Game;
import org.acme.domain.model.SalesStatistics;

import java.util.List;

public interface PublisherUseCase {
    Game publishGame(PublishGameCommand command);
    List<Game> getPublisherGames(String publisherId, int page, int size);
    SalesStatistics getGameSalesStatistics(String gameId, String publisherId);
    List<SalesStatistics> getPublisherStatistics(String publisherId);
    void updateGamePrice(String gameId, String publisherId, Double newPrice, String currency);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PublishGameCommand {
        public String title;
        public String description;
        public String shortDescription;
        public Double price;
        public String currency;
        public String developer;
        public String publisherId;
        public String releaseDate;
        public List<String> categories;
        public List<String> tags;
        public List<String> images;
        public SystemRequirementsDto systemRequirements;
        public Integer stock;
        public Boolean isPreOrderAvailable;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class SystemRequirementsDto {
        public String minimumOS;
        public String minimumProcessor;
        public String minimumMemory;
        public String minimumGraphics;
        public String minimumStorage;
        public String recommendedOS;
        public String recommendedProcessor;
        public String recommendedMemory;
        public String recommendedGraphics;
        public String recommendedStorage;
    }
}
