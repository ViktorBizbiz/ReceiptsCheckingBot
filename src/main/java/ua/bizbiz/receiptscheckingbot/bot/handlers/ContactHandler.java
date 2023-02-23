package ua.bizbiz.receiptscheckingbot.bot.handlers;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class ContactHandler {

    public List<PartialBotApiMethod<Message>> handle(Update update) {
        List<PartialBotApiMethod<Message>> responses = null;
        return responses;
    }
}
