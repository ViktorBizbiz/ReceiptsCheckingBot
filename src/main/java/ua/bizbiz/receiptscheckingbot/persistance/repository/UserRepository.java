package ua.bizbiz.receiptscheckingbot.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Role;
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsBySecretCode(Long secretCode);

    boolean existsBySecretCodeAndChatIsNull(Long secretCode);

    Optional<User> findBySecretCode(Long secretCode);

    Optional<List<User>> findAllByRoleAndChatIsNotNull(Role role);

}
