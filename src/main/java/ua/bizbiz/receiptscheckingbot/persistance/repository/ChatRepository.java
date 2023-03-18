package ua.bizbiz.receiptscheckingbot.persistance.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;

@Repository
public interface ChatRepository extends CrudRepository<Chat, Long> {

    Chat findByChatId(Long chatId);
}
