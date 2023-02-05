package ua.bizbiz.receiptscheckingbot.service;

import com.vdurmont.emoji.EmojiParser;
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
        // check if update has message
        if (update.hasMessage()) {
            // check if message has text
            if (update.getMessage().hasText()) {
                // set local variables
                Message msg = update.getMessage();
                String messageText = msg.getText();
                long chatId = msg.getChatId();

                // check message text
                if (messageText.equals("/start")) {
                    registerUser(msg);
                    startCommandGreeting(chatId, msg.getChat().getFirstName(), getMainMenuKeyboard(msg));
                } else if (messageText.equals("/sharePhoneNumber")) {
                    phoneNumberRequest(chatId);
                } else if (isAdmin(chatId)) {
                    if (messageText.equals("Додати нового користувача")) {
                        sendMessage(chatId,
                                "Відправте мені дані користувача за наступним шаблоном:\n" +
                                        "\"ПІП: Іванов Іван Іванович\"\n" +
                                        "\"Адреса: адреса_аптеки\"\n" +
                                        "\"Мережа: назва_мережі\"");
                    } else if (messageText.startsWith("ПІП:")) {
                        String fullName = messageText.substring(messageText.indexOf(" ") + 1,
                                messageText.indexOf("\nАдреса: "));
                        String address = messageText.substring(messageText.indexOf("\nАдреса: ") + "\nАдреса: ".length(),
                                messageText.indexOf("\nМережа: "));
                        String farmChain = messageText.substring(messageText.indexOf("\nМережа: ") + "\nМережа: ".length());

                        userRepository.save(User.builder()
                                .fullName(fullName)
                                .address(address)
                                .farmChain(farmChain)
                                .role(Role.USER)
                                .soldPackages(0)
                                .score(0)
                                .build());
                        deleteMessage(msg);

                        sendMessage(chatId, EmojiParser.parseToUnicode("Новий користувач був створений:white_check_mark:"));
                        //TODO Create feature to send verification code
                    }
                } else {
                    sendMessageWithReplyKeyboard(chatId, "Sorry, command was not recognized",
                            getMainMenuKeyboard(msg));
                }
            }
            // check if message has contact
            if (update.getMessage().hasContact()) {
                Message msg = update.getMessage();
                saveUserPhoneNumber(msg.getContact());
                deleteMessage(msg);
                sendMessageWithReplyKeyboard(msg.getChatId(), "Your phone number has been recorded.",
                        getMainMenuKeyboard(msg));
            }
        }
    }

    private boolean isAdmin(long chatId) {
        Optional<User> user = userRepository.findByChatId(chatId);
        return user.filter(value -> value.getRole() == Role.ADMIN).isPresent();
    }

    private SendMessage getMainMenuKeyboard(Message msg) {
        long chatId = msg.getChatId();
        // if user is Admin
        if (isAdmin(chatId)) {
            SendMessage message = new SendMessage();

            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            keyboardMarkup.setResizeKeyboard(true);

            List<KeyboardRow> keyboardRows = new ArrayList<>();

            KeyboardRow row = new KeyboardRow();
            row.add("Додати нового користувача");

            keyboardRows.add(row);

            keyboardMarkup.setKeyboard(keyboardRows);

            message.setReplyMarkup(keyboardMarkup);

            return message;
        } else {
            SendMessage message = new SendMessage();
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            keyboardMarkup.setResizeKeyboard(true);

            List<KeyboardRow> keyboardRows = new ArrayList<>();

            KeyboardRow row = new KeyboardRow();
            row.add("Phone Number");

            keyboardRows.add(row);

            keyboardMarkup.setKeyboard(keyboardRows);

            message.setReplyMarkup(keyboardMarkup);

            return message;
        }
    }

    private void deleteMessage(Message msg) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(msg.getChatId());
        deleteMessage.setMessageId(msg.getMessageId());

        deleteMessageExecutor(deleteMessage);
    }

    private void deleteMessageExecutor(DeleteMessage deleteMessage) {
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveUserPhoneNumber(Contact contact) {
        Optional<User> oldUser = userRepository.findByChatId(contact.getUserId());
        if (oldUser.isPresent()) {
            var existingUser = oldUser.get();
            User updatedUser = User.builder()
                    .userId(existingUser.getUserId())
                    .chatId(existingUser.getChatId())
                    .firstName(existingUser.getFirstName())
                    .lastName(existingUser.getLastName())
                    .userName(existingUser.getUserName())
                    .phoneNumber(contact.getPhoneNumber())
                    .registeredAt(existingUser.getRegisteredAt())
                    .soldPackages(existingUser.getSoldPackages())
                    .score(existingUser.getScore())
                    .role(existingUser.getRole())
                    .fullName(existingUser.getFullName())
                    .address(existingUser.getAddress())
                    .farmChain(existingUser.getFarmChain())
                    .build();
            userRepository.save(updatedUser);
        }
    }

    private void phoneNumberRequest(long chatId) {
        SendMessage message = new SendMessage();

        message.setChatId(chatId);
        message.setText("Put on the button: Share your number >");

        // create keyboard
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        message.setReplyMarkup(replyKeyboardMarkup);
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

        sendMessageExecutor(message);
    }

    private void sendMessageExecutor(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerUser(Message msg) {

        if (userRepository.findByChatId(msg.getChatId()).isEmpty()) {
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

    private void startCommandGreeting(long chatId, String name, SendMessage message) {
        String answer = "Hi, " + name + ", nice to meet you there!\n\n" +
                "Could you send me your phone number? This definitely help me to improve my work.\n" +
                "/sharePhoneNumber";
        sendMessageWithReplyKeyboard(chatId, answer, message);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        sendMessageExecutor(message);
    }

    private void sendMessageWithReplyKeyboard(long chatId, String textToSend, SendMessage message) {
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        sendMessageExecutor(message);
    }

    @Scheduled(cron = "0 0 9 * * *")
    @Scheduled(cron = "0 0 16 * * *")
    private void sendMotivationText() {
        var users = userRepository.findAll();
        for (User user : users) {
            String motivationText = "Hello, " + user.getFirstName() + "! Ничто так не согревает холодными ночами, как мысли о тебе))";
            sendMessage(user.getChatId(), motivationText);
        }
    }
}
