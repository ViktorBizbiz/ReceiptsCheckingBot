package ua.bizbiz.receiptscheckingbot.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByChatId(Long chatId);

    boolean existsBySecretCode(Long secretCode);

    Optional<User> findBySecretCode(Long secretCode);
}
