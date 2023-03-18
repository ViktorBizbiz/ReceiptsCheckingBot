package ua.bizbiz.receiptscheckingbot.bot;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.bizbiz.receiptscheckingbot.bot.handlers.CallbackHandler;
import ua.bizbiz.receiptscheckingbot.bot.handlers.MessageHandler;
import ua.bizbiz.receiptscheckingbot.bot.handlers.PhotoHandler;
import ua.bizbiz.receiptscheckingbot.config.BotConfig;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;
import ua.bizbiz.receiptscheckingbot.util.DataHolder;
import ua.bizbiz.receiptscheckingbot.util.PhotoMessageData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final UserRepository userRepository;
    private final MessageHandler messageHandler;
    private final CallbackHandler callbackHandler;
    private final PhotoHandler photoHandler;
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

        List<Validable> responses = new ArrayList<>();

        if (update.hasCallbackQuery()) {
            responses = callbackHandler.handle(update);
        } else if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                responses = messageHandler.handle(update);
            } else if (update.getMessage().hasPhoto()) {
                responses = photoHandler.handle(update);
            }
        }
        for (Validable response : responses) {
            if (response instanceof SendMessage)
                execute((SendMessage) response);
            else if (response instanceof SendPhoto)
                executeAndRememberMessageId((SendPhoto) response);
            else if (response instanceof SendDocument)
                execute((SendDocument) response);
            else if (response instanceof EditMessageReplyMarkup)
                execute((EditMessageReplyMarkup) response);
            else if (response instanceof DeleteMessage)
                execute((DeleteMessage) response);
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
}
