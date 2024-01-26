package ua.bizbiz.receiptscheckingbot.bot.processor.photo;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;

import java.time.LocalDateTime;
import java.util.List;

public interface PhotoProcessor {

    List<Validable> process(Chat chat, Message msg, LocalDateTime dateTime);
}
