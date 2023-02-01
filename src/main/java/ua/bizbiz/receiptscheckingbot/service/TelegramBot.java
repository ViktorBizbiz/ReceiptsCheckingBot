package ua.bizbiz.receiptscheckingbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.bizbiz.receiptscheckingbot.config.BotConfig;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Role;
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;

    private final UserRepository userRepository;

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message msg = update.getMessage();
            String messageText = msg.getText();
            long chatId = msg.getChatId();

            switch (messageText) {
                case "/start":
                    registerUser(msg);
                    startCommandGreeting(chatId, msg.getChat().getFirstName());
                    break;
                case "/sharePhoneNumber":
                    phoneNumberRequest(chatId);
                    break;
                default:
                    sendMessage(chatId, "Sorry, command was not recognized");
            }
        }
        if (update.getMessage().hasContact()) {
            saveUserPhoneNumber(update.getMessage().getContact());
            deleteMessage(update.getMessage());
        }
    }

    private void deleteMessage(Message msg) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(msg.getChatId());
        deleteMessage.setMessageId(msg.getMessageId());
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveUserPhoneNumber(Contact contact) {
        Optional<User> oldUser = userRepository.findById(contact.getUserId());
        if (oldUser.isPresent()) {
            var existingUser = oldUser.get();
            User updatedUser = User.builder()
                    .chatId(existingUser.getChatId())
                    .firstName(existingUser.getFirstName())
                    .lastName(existingUser.getLastName())
                    .userName(existingUser.getUserName())
                    .phoneNumber(contact.getPhoneNumber())
                    .registeredAt(existingUser.getRegisteredAt())
                    .soldPackages(0)
                    .score(0)
                    .role(Role.USER)
                    .build();
            userRepository.save(updatedUser);
        }
    }

    private void phoneNumberRequest(long chatId) {
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(chatId);
        sendMessage.setText("You send /sharePhoneNumber");

        // create keyboard
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        // new list
        List<KeyboardRow> keyboard = new ArrayList<>();

        // first keyboard line
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText("Share your number >");
        keyboardButton.setRequestContact(true);
        keyboardFirstRow.add(keyboardButton);

        // add array to list
        keyboard.add(keyboardFirstRow);

        // add list to our keyboard
        replyKeyboardMarkup.setKeyboard(keyboard);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerUser(Message msg) {

        if (userRepository.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = User.builder()
                    .chatId(chatId)
                    .firstName(chat.getFirstName())
                    .lastName(chat.getLastName())
                    .userName(chat.getUserName())
                    .phoneNumber(null)
                    .registeredAt(new Timestamp(System.currentTimeMillis()))
                    .soldPackages(0)
                    .score(0)
                    .role(Role.USER)
                    .build();

            userRepository.save(user);
        }
    }

    private void startCommandGreeting(long chatId, String name) {
        String answer = "Hi, " + name + ", nice to meet you there!\n\n" +
                "Could you send me your phone number? This definitely help me to improve my work.\n" +
                "/sharePhoneNumber";
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    @Scheduled(cron = "0 0 16 * * *")
    private void sendMotivationText() {
        var users = userRepository.findAll();
        for (User user : users) {
            String motivation = "Hello, " + user.getFirstName() + "! Ничто так не согревает холодными ночами, как мысли о тебе))";
            sendMessage(user.getChatId(), motivation);
        }
    }
}
