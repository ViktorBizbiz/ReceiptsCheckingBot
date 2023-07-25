package ua.bizbiz.receiptscheckingbot.bot.processor.text.message;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.message.impl.AnnouncementProcessor;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.message.impl.PromotionProcessor;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.message.impl.SecretCodeProcessor;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.message.impl.UserProcessor;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.repository.ChatRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.PromotionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.SubscriptionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class MessageProcessorFactory {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final PromotionRepository promotionRepository;
    private final SubscriptionRepository subscriptionRepository;

    public MessageProcessor createMessageProcessor(Chat chat) {

        switch (chat.getStatus()) {
            case CREATING_USER, READING_USER, UPDATING_USER, DELETING_USER -> {
                return new UserProcessor(userRepository);
            }
            case ENTERING_SECRET_CODE -> {
                return new SecretCodeProcessor(userRepository);
            }
            case SENDING_ANNOUNCEMENT_TO_ALL, SENDING_ANNOUNCEMENT_TO_PERSON -> {
                return new AnnouncementProcessor(userRepository);
            }
            case CREATING_PROMOTION, UPDATING_PROMOTION, DELETING_PROMOTION -> {
                return new PromotionProcessor(promotionRepository);
            }
            default -> {
                return null;
            }
        }
    }
}
