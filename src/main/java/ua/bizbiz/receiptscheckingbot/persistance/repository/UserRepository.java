package ua.bizbiz.receiptscheckingbot.persistance.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

}
