package ua.bizbiz.receiptscheckingbot.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    @Transactional
    void deleteByPromotionId(Long promotionId);
}
