package org.acme.application.port.in;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acme.domain.model.Bundle;

import java.util.List;

public interface BundleUseCase {
    Bundle createBundle(CreateBundleCommand command);
    Bundle getBundleById(String id);
    List<Bundle> getActiveBundles();
    List<Bundle> getBundlesByGame(String gameId);
    void deactivateBundle(String bundleId);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CreateBundleCommand {
        public String name;
        public String description;
        public List<String> gameIds;
        public Double originalPrice;
        public Double bundlePrice;
        public String currency;
        public String validUntil;
    }
}
