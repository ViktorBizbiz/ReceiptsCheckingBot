package ua.bizbiz.receiptscheckingbot.persistance.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Role;
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByChatId(Long chatId);

    boolean existsBySecretCode(Long secretCode);

    Optional<User> findBySecretCode(Long secretCode);

    Optional<List<User>> findAllByRoleAndChatIsNotNull(Role role);
}
