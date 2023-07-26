package ua.bizbiz.receiptscheckingbot.bot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.bizbiz.receiptscheckingbot.bot.processor.photo.PhotoProcessorFactory;
import ua.bizbiz.receiptscheckingbot.persistance.repository.ChatRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PhotoHandler implements UpdateHandler {

    private final ChatRepository chatRepository;
    private final PhotoProcessorFactory factory;

    public List<Validable> handle(Update update) {
        final var chat = chatRepository.findByChatId(update.getMessage().getChatId());
        final var message = update.getMessage();
        final List<Validable> responses = factory.getPhotoProcessor(chat).process(chat, message);

        log.info("Update handling with status: " + chat.getStatus());

        chatRepository.save(chat);
        return responses;
    }
}
