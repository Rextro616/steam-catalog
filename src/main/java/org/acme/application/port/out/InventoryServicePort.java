package org.acme.application.port.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryServicePort {
    void addGameToLibrary(AddGameRequest request);
    boolean userOwnsGame(String userId, String gameId);
    List<String> getUserGames(String userId);
    void removeGameFromLibrary(String userId, String gameId);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class AddGameRequest {
        public String userId;
        public String gameId;
        public String gameName;
        public String acquisitionType;
        public String transactionId;
        public LocalDateTime acquiredAt;
    }
}
