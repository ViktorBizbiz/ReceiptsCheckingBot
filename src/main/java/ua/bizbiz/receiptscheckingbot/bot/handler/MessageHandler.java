package ua.bizbiz.receiptscheckingbot.bot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.CommandType;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.command.CommandParser;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.command.CommandProcessorFactory;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.message.MessageProcessorFactory;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.repository.ChatRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHandler implements UpdateHandler {

    private final ChatRepository chatRepository;
    private final MessageProcessorFactory messageProcessorFactory;
    private final CommandProcessorFactory commandProcessorFactory;
    private final CommandParser commandParser;

    public List<Validable> handle(Update update) {
        final var chat = provideChatRecord(update.getMessage().getChatId());
        final var text = update.getMessage().getText();
        List<Validable> responses;

        CommandType command;
        if ((command = commandParser.parse(chat, text)) != null) {
            responses = commandProcessorFactory.getCommandProcessor(command).process(chat, command);
        } else {
            responses = messageProcessorFactory.getMessageProcessor(chat).process(chat, text);
        }

        log.info("Update handling with status: " + chat.getStatus());

        chatRepository.save(chat);
        return responses;
    }

    private Chat provideChatRecord(Long chatId) {
        var chat = chatRepository.findByChatId(chatId);
        if (chat == null) {
            chat = Chat.builder()
                    .chatId(chatId)
                    .build();
            chatRepository.save(chat);
        }
        return chat;
    }
}
