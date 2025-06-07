package org.acme.domain.model.valueobjects;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Value
@Builder
public class Rating {
    BigDecimal value;
    Integer totalVotes;

    public Rating(BigDecimal value, Integer totalVotes) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new IllegalArgumentException("La calificación debe estar entre 0 y 5");
        }
        if (totalVotes == null || totalVotes < 0) {
            throw new IllegalArgumentException("El total de votos no puede ser negativo");
        }
        this.value = value.setScale(2, RoundingMode.HALF_UP);
        this.totalVotes = totalVotes;
    }

    public static Rating of(double value, int totalVotes) {
        return new Rating(BigDecimal.valueOf(value), totalVotes);
    }

    public static Rating excellent() {
        return Rating.of(5.0, 0);
    }

    public static Rating good() {
        return Rating.of(4.0, 0);
    }

    public static Rating average() {
        return Rating.of(3.0, 0);
    }

    public static Rating poor() {
        return Rating.of(2.0, 0);
    }

    public static Rating terrible() {
        return Rating.of(1.0, 0);
    }

    public static Rating unrated() {
        return Rating.of(0.0, 0);
    }

    public Rating addVote(BigDecimal newRating) {
        if (newRating.compareTo(BigDecimal.ZERO) < 0 || newRating.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new IllegalArgumentException("La nueva calificación debe estar entre 0 y 5");
        }

        BigDecimal totalScore = value.multiply(BigDecimal.valueOf(totalVotes));
        totalScore = totalScore.add(newRating);
        int newTotalVotes = totalVotes + 1;
        BigDecimal newAverageRating = totalScore.divide(BigDecimal.valueOf(newTotalVotes), 2, RoundingMode.HALF_UP);

        return new Rating(newAverageRating, newTotalVotes);
    }

    public String getStarRating() {
        int stars = value.intValue();
        boolean hasHalfStar = value.remainder(BigDecimal.ONE).compareTo(BigDecimal.valueOf(0.5)) >= 0;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stars; i++) {
            sb.append("★");
        }
        if (hasHalfStar && stars < 5) {
            sb.append("☆");
            stars++;
        }
        for (int i = stars; i < 5; i++) {
            sb.append("☆");
        }
        return sb.toString();
    }

    public String getQualityDescription() {
        double val = value.doubleValue();
        if (val >= 4.5) return "Excelente";
        if (val >= 4.0) return "Muy Bueno";
        if (val >= 3.5) return "Bueno";
        if (val >= 3.0) return "Regular";
        if (val >= 2.0) return "Malo";
        if (val >= 1.0) return "Muy Malo";
        return "Sin Calificación";
    }

    public boolean isExcellent() {
        return value.compareTo(BigDecimal.valueOf(4.5)) >= 0;
    }

    public boolean isGood() {
        return value.compareTo(BigDecimal.valueOf(3.5)) >= 0;
    }

    public boolean isPoor() {
        return value.compareTo(BigDecimal.valueOf(2.5)) < 0;
    }

    public boolean hasEnoughVotes() {
        return totalVotes >= 10; // Minimum votes to be considered reliable
    }

    public boolean isReliable() {
        return hasEnoughVotes() && value.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public String toString() {
        return String.format("%.2f/5 (%d %s)",
                value,
                totalVotes,
                totalVotes == 1 ? "voto" : "votos");
    }
}