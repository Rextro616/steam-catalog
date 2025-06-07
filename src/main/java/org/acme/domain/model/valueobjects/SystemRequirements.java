package org.acme.domain.model.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class SystemRequirements {
    String minimumOS;
    String minimumProcessor;
    String minimumMemory;
    String minimumGraphics;
    String minimumStorage;
    String recommendedOS;
    String recommendedProcessor;
    String recommendedMemory;
    String recommendedGraphics;
    String recommendedStorage;

    public static SystemRequirements basic() {
        return SystemRequirements.builder()
                .minimumOS("Windows 10 64-bit")
                .minimumProcessor("Intel Core i3 or AMD equivalent")
                .minimumMemory("4 GB RAM")
                .minimumGraphics("DirectX 11 compatible")
                .minimumStorage("1 GB available space")
                .recommendedOS("Windows 11 64-bit")
                .recommendedProcessor("Intel Core i5 or AMD equivalent")
                .recommendedMemory("8 GB RAM")
                .recommendedGraphics("DirectX 12 compatible")
                .recommendedStorage("2 GB available space")
                .build();
    }

    public static SystemRequirements demanding() {
        return SystemRequirements.builder()
                .minimumOS("Windows 10 64-bit")
                .minimumProcessor("Intel Core i5-8400 / AMD Ryzen 5 2600")
                .minimumMemory("8 GB RAM")
                .minimumGraphics("NVIDIA GTX 1060 / AMD RX 580")
                .minimumStorage("50 GB available space")
                .recommendedOS("Windows 11 64-bit")
                .recommendedProcessor("Intel Core i7-9700K / AMD Ryzen 7 3700X")
                .recommendedMemory("16 GB RAM")
                .recommendedGraphics("NVIDIA RTX 3070 / AMD RX 6700 XT")
                .recommendedStorage("50 GB SSD space")
                .build();
    }

    public boolean hasMinimumRequirements() {
        return minimumOS != null && !minimumOS.trim().isEmpty() &&
                minimumProcessor != null && !minimumProcessor.trim().isEmpty() &&
                minimumMemory != null && !minimumMemory.trim().isEmpty() &&
                minimumGraphics != null && !minimumGraphics.trim().isEmpty() &&
                minimumStorage != null && !minimumStorage.trim().isEmpty();
    }

    public boolean hasRecommendedRequirements() {
        return recommendedOS != null && !recommendedOS.trim().isEmpty() &&
                recommendedProcessor != null && !recommendedProcessor.trim().isEmpty() &&
                recommendedMemory != null && !recommendedMemory.trim().isEmpty() &&
                recommendedGraphics != null && !recommendedGraphics.trim().isEmpty() &&
                recommendedStorage != null && !recommendedStorage.trim().isEmpty();
    }

    public boolean isComplete() {
        return hasMinimumRequirements() && hasRecommendedRequirements();
    }

    public String getMinimumStorageInGB() {
        if (minimumStorage == null) return "0";
        return extractStorageNumber(minimumStorage);
    }

    public String getRecommendedStorageInGB() {
        if (recommendedStorage == null) return "0";
        return extractStorageNumber(recommendedStorage);
    }

    private String extractStorageNumber(String storage) {
        // Simple extraction - in real implementation would be more robust
        return storage.replaceAll("[^0-9]", "");
    }

    public boolean supportsDirectX12() {
        return (minimumGraphics != null && minimumGraphics.toLowerCase().contains("directx 12")) ||
                (recommendedGraphics != null && recommendedGraphics.toLowerCase().contains("directx 12"));
    }

    public boolean requiresSSD() {
        return (minimumStorage != null && minimumStorage.toLowerCase().contains("ssd")) ||
                (recommendedStorage != null && recommendedStorage.toLowerCase().contains("ssd"));
    }
}