package ua.bizbiz.receiptscheckingbot.bot.handler;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface UpdateHandler {
    List<Validable> handle(Update update);
}
