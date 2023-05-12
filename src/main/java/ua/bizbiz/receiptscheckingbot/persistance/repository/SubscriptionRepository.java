package ua.bizbiz.receiptscheckingbot.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Subscription;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    @Transactional
    @Modifying
    @Query(
            value = "DELETE FROM subscriptions WHERE promotion_id = :promotionId AND user_id = :userId",
            nativeQuery = true
    )
    void deleteByPromotionIdAndUserId(@Param("promotionId") Long promotionId,
                                      @Param("userId") Long userId);

    List<Subscription> findAllByUserId(Long userId);
}