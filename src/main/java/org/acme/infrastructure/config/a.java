package org.acme.infrastructure.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.acme.application.port.in.*;
import org.acme.application.service.GameApplicationService;
import org.acme.application.service.GiftApplicationService;
import org.acme.application.service.PreOrderApplicationService;
import org.acme.domain.repository.*;
import org.acme.infrastructure.adapter.out.persistence.repository.GameRepositoryAdapter;

@ApplicationScoped
public class BeanConfig {

    @Produces
    @Singleton
    public GameUseCase gameUseCase(GameApplicationService gameApplicationService) {
        return gameApplicationService;
    }

    @Produces
    @Singleton
    public PreOrderUseCase preOrderUseCase(PreOrderApplicationService preOrderApplicationService) {
        return preOrderApplicationService;
    }

    @Produces
    @Singleton
    public GiftUseCase giftUseCase(GiftApplicationService giftApplicationService) {
        return giftApplicationService;
    }

    @Produces
    @Singleton
    public PublisherUseCase publisherUseCase(PublisherApplicationService publisherApplicationService) {
        return publisherApplicationService;
    }

    @Produces
    @Singleton
    public OfferUseCase offerUseCase(OfferApplicationService offerApplicationService) {
        return offerApplicationService;
    }

    @Produces
    @Singleton
    public ReviewUseCase reviewUseCase(ReviewApplicationService reviewApplicationService) {
        return reviewApplicationService;
    }

    @Produces
    @Singleton
    public BundleUseCase bundleUseCase(BundleApplicationService bundleApplicationService) {
        return bundleApplicationService;
    }

    // Repository Configurations
    @Produces
    @Singleton
    public GameRepository gameRepository(GameRepositoryAdapter gameRepositoryAdapter) {
        return gameRepositoryAdapter;
    }

    @Produces
    @Singleton
    public PreOrderRepository preOrderRepository(PreOrderRepositoryAdapter preOrderRepositoryAdapter) {
        return preOrderRepositoryAdapter;
    }

    @Produces
    @Singleton
    public GiftRepository giftRepository(GiftRepositoryAdapter giftRepositoryAdapter) {
        return giftRepositoryAdapter;
    }

    @Produces
    @Singleton
    public ReviewRepository reviewRepository(ReviewRepositoryAdapter reviewRepositoryAdapter) {
        return reviewRepositoryAdapter;
    }

    @Produces
    @Singleton
    public OfferRepository offerRepository(OfferRepositoryAdapter offerRepositoryAdapter) {
        return offerRepositoryAdapter;
    }

    @Produces
    @Singleton
    public SalesStatisticsRepository salesStatisticsRepository(SalesStatisticsRepositoryAdapter salesStatisticsRepositoryAdapter) {
        return salesStatisticsRepositoryAdapter;
    }

    @Produces
    @Singleton
    public BundleRepository bundleRepository(BundleRepositoryAdapter bundleRepositoryAdapter) {
        return bundleRepositoryAdapter;
    }

    @Produces
    @Singleton
    public DLCRepository dlcRepository(DLCRepositoryAdapter dlcRepositoryAdapter) {
        return dlcRepositoryAdapter;
    }
}