package ua.bizbiz.receiptscheckingbot.bot.commands;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;

public interface ProcessableCommand {
    PartialBotApiMethod<Message> process(Chat chat);
}
