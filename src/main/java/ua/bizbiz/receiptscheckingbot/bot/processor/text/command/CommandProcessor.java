package ua.bizbiz.receiptscheckingbot.bot.processor.text.command;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.CommandType;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.Markable;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;

import java.util.List;

public interface CommandProcessor {
    List<Validable> process(Chat chat, CommandType command);

    Class<? extends Markable> getMarkClass();
}
