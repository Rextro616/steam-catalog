package org.acme.domain.repository;

import org.acme.domain.model.Gift;
import java.util.List;
import java.util.Optional;

public interface GiftRepository {
    Gift save(Gift gift);
    Optional<Gift> findById(String id);
    List<Gift> findBySenderId(String senderId);
    List<Gift> findByRecipientId(String recipientId);
    List<Gift> findPendingByRecipientId(String recipientId);
    List<Gift> findByStatus(String status);
    List<Gift> findExpiredGifts();
    void delete(String id);
    long count();
    long countByRecipientId(String recipientId);
    boolean existsById(String id);
}