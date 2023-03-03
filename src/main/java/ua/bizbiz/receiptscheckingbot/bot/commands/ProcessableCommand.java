package ua.bizbiz.receiptscheckingbot.bot.commands;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;

public interface ProcessableCommand {
    Validable process(Chat chat);
}
