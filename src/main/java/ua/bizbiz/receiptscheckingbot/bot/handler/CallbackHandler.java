package ua.bizbiz.receiptscheckingbot.bot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.bizbiz.receiptscheckingbot.bot.processor.callback.CallbackProcessorFactory;
import ua.bizbiz.receiptscheckingbot.persistance.repository.ChatRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallbackHandler implements UpdateHandler {

    private final ChatRepository chatRepository;
    private final CallbackProcessorFactory factory;

    public List<Validable> handle(Update update) {
        final var callbackQuery = update.getCallbackQuery();
        final var callbackData = callbackQuery.getData().split("\n");
        final var message = callbackQuery.getMessage();
        final var chat = chatRepository.findByChatId(callbackQuery.getMessage().getChatId());

        final List<Validable> responses = factory.getCallbackProcessor(chat, callbackData)
                .process(chat, callbackData, message);

        log.info("Update handling with status: " + chat.getStatus());

        chatRepository.save(chat);
        return responses;
    }
}
