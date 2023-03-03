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
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.bizbiz.receiptscheckingbot.bot.handlers.CallbackHandler;
import ua.bizbiz.receiptscheckingbot.bot.handlers.MessageHandler;
import ua.bizbiz.receiptscheckingbot.config.BotConfig;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;

    private final UserRepository userRepository;

    private final MessageHandler messageHandler;

    private final CallbackHandler callbackHandler;

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
            }
        }
        for (Validable response : responses) {
            if (response instanceof SendMessage)
                execute((SendMessage) response);
            else if (response instanceof SendPhoto)
                execute((SendPhoto) response);
            else if (response instanceof SendDocument)
                execute((SendDocument) response);
            else if (response instanceof EditMessageReplyMarkup)
                execute((EditMessageReplyMarkup) response);
            else if (response instanceof DeleteMessage)
                execute((DeleteMessage) response);
        }
        /**if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            // define your command chain here
            List<String> commandChain = Arrays.asList("Додати нового користувача \uD83D\uDC64", "", "◀️ Назад");

            // check if the message text matches the last command in the chain
            if (commandChain.contains(messageText) && commandChain.indexOf(messageText) == commandChain.size() - 1) {
                int lastCommandIndex = commandChain.lastIndexOf(messageText);
                Long chatId = update.getMessage().getChatId();
                Integer messageId = update.getMessage().getMessageId();


                // delete all messages above the last command in the chain
                for (int i = messageId; i >= messageId - (lastCommandIndex + 1); i--) {
                    DeleteMessage deleteMessage = new DeleteMessage(chatId.toString(), i);
                    execute(deleteMessage);
                }

                // add your logic for handling the command chain here
            }
        }*/
    }
}
