package ua.bizbiz.receiptscheckingbot.bot;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.bizbiz.receiptscheckingbot.bot.handler.UpdateHandlerFactory;
import ua.bizbiz.receiptscheckingbot.config.bot.BotConfig;
import ua.bizbiz.receiptscheckingbot.util.DataHolder;
import ua.bizbiz.receiptscheckingbot.util.PhotoMessageData;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final UpdateHandlerFactory factory;
    private final DataHolder dataHolder;

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        List<Validable> responses = factory.getUpdateHandler(update).handle(update);

        for (Validable response : responses) {
            if (response instanceof SendMessage sendMessage)
                execute(sendMessage);
            else if (response instanceof SendPhoto sendPhoto)
                executeAndRememberMessageId(sendPhoto);
            else if (response instanceof SendDocument sendDocument)
                execute(sendDocument);
            else if (response instanceof EditMessageReplyMarkup editMessageReplyMarkup)
                execute(editMessageReplyMarkup);
            else if (response instanceof DeleteMessage deleteMessage)
                execute(deleteMessage);
        }
    }

    @SneakyThrows
    private void executeAndRememberMessageId(SendPhoto response) {
        Message sentPhoto = execute(response);

        Long chatId = sentPhoto.getChatId();
        Integer messageId = sentPhoto.getMessageId();

        List<PhotoMessageData> photoMessages = dataHolder.getPhotoMessages();
        LocalDateTime photoCreationTime = dataHolder.getPhotoCreationTime();

        photoMessages.add(new PhotoMessageData(messageId, chatId, photoCreationTime));

        dataHolder.setPhotoMessages(photoMessages);
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) throws TelegramApiException {
        return super.execute(method);
    }
}
