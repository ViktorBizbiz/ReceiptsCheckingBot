package ua.bizbiz.receiptscheckingbot.persistance.repository;

import org.springframework.data.repository.CrudRepository;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;

public interface ChatRepository extends CrudRepository<Chat, Long> {

    Chat findByChatId(Long chatId);
}
