package org.acme.domain.model.valueobjects;

import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder
public class Category {
    String name;
    String description;

    public Category(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede ser nulo o vacío");
        }
        this.name = name.trim();
        this.description = description != null ? description.trim() : "";
    }

    public static Category of(String name) {
        return new Category(name, "");
    }

    public static Category of(String name, String description) {
        return new Category(name, description);
    }

    public static final Category ACTION = Category.of("Accion", "Juegos de acción y aventura");
    public static final Category RPG = Category.of("RPG", "Juegos de rol");
    public static final Category STRATEGY = Category.of("Estrategia", "Juegos de estrategia");
    public static final Category SIMULATION = Category.of("Simulacion", "Juegos de simulación");
    public static final Category SPORTS = Category.of("Deportes", "Juegos deportivos");
    public static final Category RACING = Category.of("Carreras", "Juegos de carreras");
    public static final Category ADVENTURE = Category.of("Aventura", "Juegos de aventura");
    public static final Category INDIE = Category.of("Indie", "Juegos independientes");
    public static final Category CASUAL = Category.of("Casual", "Juegos casuales");
    public static final Category HORROR = Category.of("Terror", "Juegos de terror");

    public static Set<Category> getDefaultCategories() {
        return Set.of(ACTION, RPG, STRATEGY, SIMULATION, SPORTS, RACING, ADVENTURE, INDIE, CASUAL, HORROR);
    }

    public boolean isAction() {
        return "Accion".equalsIgnoreCase(name);
    }

    public boolean isRPG() {
        return "RPG".equalsIgnoreCase(name);
    }

    public boolean isStrategy() {
        return "Estrategia".equalsIgnoreCase(name);
    }

    public boolean isIndie() {
        return "Indie".equalsIgnoreCase(name);
    }

    @Override
    public String toString() {
        return name;
    }
}