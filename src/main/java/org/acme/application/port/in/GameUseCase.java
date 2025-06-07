package org.acme.application.port.in;

import org.acme.domain.model.Game;
import org.acme.domain.model.valueobjects.GameId;
import org.acme.domain.model.valueobjects.Category;
import lombok.*;
import java.util.List;

public interface GameUseCase {
    Game createGame(CreateGameCommand command);
    Game getGameById(GameId id);
    List<Game> getAllGames(int page, int size);
    List<Game> getGamesByCategory(Category category, int page, int size);
    Game updateGame(UpdateGameCommand command);
    void deleteGame(GameId id);
    List<Game> searchGames(String title, int page, int size);
    List<Game> getTopRatedGames(int limit);
    List<Game> getRecentlyAddedGames(int limit);
    List<Game> getDiscountedGames();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CreateGameCommand {
        public String title;
        public String description;
        public String shortDescription;
        public Double price;
        public String currency;
        public String developer;
        public String publisher;
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
    class UpdateGameCommand {
        public String id;
        public String title;
        public String description;
        public String shortDescription;
        public Double price;
        public String currency;
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