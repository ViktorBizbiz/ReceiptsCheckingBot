package ua.bizbiz.receiptscheckingbot.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Promotion;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
}
