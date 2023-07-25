package ua.bizbiz.receiptscheckingbot.bot.processor.text.message;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;

import java.util.List;

public interface MessageProcessor {

    List<Validable> process(Chat chat, String text);
}
