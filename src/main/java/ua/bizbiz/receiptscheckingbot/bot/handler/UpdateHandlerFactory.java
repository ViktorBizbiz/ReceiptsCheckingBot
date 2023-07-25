package ua.bizbiz.receiptscheckingbot.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.command.CommandParser;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.command.CommandProcessorFactory;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.message.MessageProcessorFactory;
import ua.bizbiz.receiptscheckingbot.persistance.repository.ChatRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.PromotionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.SubscriptionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;
import ua.bizbiz.receiptscheckingbot.util.DataHolder;

@Component
@RequiredArgsConstructor
public class UpdateHandlerFactory {

    private final ChatRepository chatRepository;
    private final PromotionRepository promotionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final DataHolder dataHolder;
    private final MessageProcessorFactory messageProcessorFactory;
    private final CommandProcessorFactory commandProcessorFactory;
    private final CommandParser commandParser;
    private final UserRepository userRepository;


    public UpdateHandler createUpdateHandler(Update update) {
        if (update.hasCallbackQuery()) {
            return new CallbackHandler(chatRepository,
                    promotionRepository,
                    subscriptionRepository,
                    dataHolder);
        } else if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                return new MessageHandler(chatRepository,
                        messageProcessorFactory,
                        commandProcessorFactory,
                        commandParser);
            } else if (update.getMessage().hasPhoto()) {
                return new PhotoHandler(chatRepository,
                        userRepository,
                        subscriptionRepository,
                        dataHolder);
            }
        }
        return null;
    }
}
