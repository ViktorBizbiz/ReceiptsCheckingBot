package ua.bizbiz.receiptscheckingbot.bot.processor.text.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.CommandType;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.ChatStatus;

@Component
@RequiredArgsConstructor
public class CommandParser {

    private final CommandProcessorFactory commandProcessorFactory;

    public boolean isCommand(Chat chat, String text) {
        return parse(chat, text) != null;
    }

    public CommandType parse(Chat chat, String text) {
        var commandOptional = CommandType.parse(text);
        return commandOptional
                .filter(commandType -> ChatStatus.DEFAULT.equals(commandType.getStatus()))
                .orElse(commandOptional
                        .filter(commandType -> chat.getStatus().equals(commandType.getStatus()))
                        .orElse(null));
    }
}
