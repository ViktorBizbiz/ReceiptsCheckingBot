package ua.bizbiz.receiptscheckingbot.bot.processor.callback.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.CommandType;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu.HomeCommand;
import ua.bizbiz.receiptscheckingbot.bot.processor.callback.CallbackProcessor;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.util.DeleteUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class HomeProcessor implements CallbackProcessor {
    @Override
    public List<Validable> process(Chat chat, String[] callbackData, Message msg) {
        final List<Validable> responses = new ArrayList<>();
        final var messageId = msg.getMessageId();
        final var text = callbackData[0];
        if (text.equalsIgnoreCase(CommandType.HOME.getName())) {
            log.info("HomeCommandType detected: " + CommandType.HOME);
            responses.addAll(DeleteUtils.deleteMessages(messageId, 1, chat));
            responses.add(new HomeCommand(chat).process(chat));
        }
        return responses;
    }
}
