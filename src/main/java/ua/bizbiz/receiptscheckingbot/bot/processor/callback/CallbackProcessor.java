package ua.bizbiz.receiptscheckingbot.bot.processor.callback;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;

import java.util.List;

public interface CallbackProcessor {

    List<Validable> process(Chat chat, String[] callbackData, Message msg);
}
