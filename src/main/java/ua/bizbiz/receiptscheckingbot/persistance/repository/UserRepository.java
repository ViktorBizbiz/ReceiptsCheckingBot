package ua.bizbiz.receiptscheckingbot.persistance.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Role;
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    boolean existsBySecretCode(Long secretCode);

    boolean existsBySecretCodeAndChatIsNull(Long secretCode);

    Optional<User> findBySecretCode(Long secretCode);

    Optional<List<User>> findAllByRoleAndChatIsNotNull(Role role);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_promotion WHERE user_id = :userId AND promotion_id = :promotionId", nativeQuery = true)
    void deletePromotionFromUser(@Param("userId") Long userId, @Param("promotionId") Long promotionId);
}
