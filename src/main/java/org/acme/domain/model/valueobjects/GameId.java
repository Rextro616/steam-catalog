package org.acme.domain.model.valueobjects;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class GameId {
    String value;

    public GameId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("GameId no puede ser nulo o vacío");
        }
        this.value = value;
    }

    public static GameId generate() {
        return new GameId(UUID.randomUUID().toString());
    }

    public static GameId of(String value) {
        return new GameId(value);
    }

    public boolean isValid() {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return value;
    }
}