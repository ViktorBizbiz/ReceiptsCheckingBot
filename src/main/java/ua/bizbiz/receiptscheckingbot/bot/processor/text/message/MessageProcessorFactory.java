package ua.bizbiz.receiptscheckingbot.bot.processor.text.message;

import org.springframework.stereotype.Component;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.message.impl.AnnouncementProcessor;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.message.impl.PromotionProcessor;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.message.impl.SecretCodeProcessor;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.message.impl.UserProcessor;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MessageProcessorFactory {

    private final Map<Class<? extends MessageProcessor>, MessageProcessor> messageProcessorMap;

    public MessageProcessorFactory(List<MessageProcessor> messageProcessors) {
        this.messageProcessorMap = messageProcessors.stream()
                .collect(Collectors.toMap(MessageProcessor::getClass, Function.identity()));
    }

    public MessageProcessor getMessageProcessor(Chat chat) {

        switch (chat.getStatus()) {
            case CREATING_USER, READING_USER, UPDATING_USER, DELETING_USER -> {
                return messageProcessorMap.get(UserProcessor.class);
            }
            case ENTERING_SECRET_CODE -> {
                return messageProcessorMap.get(SecretCodeProcessor.class);
            }
            case SENDING_ANNOUNCEMENT_TO_ALL, SENDING_ANNOUNCEMENT_TO_PERSON -> {
                return messageProcessorMap.get(AnnouncementProcessor.class);
            }
            case CREATING_PROMOTION, UPDATING_PROMOTION, DELETING_PROMOTION -> {
                return messageProcessorMap.get(PromotionProcessor.class);
            }
            default -> {
                return null;
            }
        }
    }
}
