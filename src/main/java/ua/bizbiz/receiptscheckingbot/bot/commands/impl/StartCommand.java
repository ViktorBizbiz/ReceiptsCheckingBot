package ua.bizbiz.receiptscheckingbot.bot.commands.impl;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ua.bizbiz.receiptscheckingbot.bot.commands.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.MainCommandType;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.ChatStatus;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Role;

public class StartCommand implements ProcessableCommand {

    private final String responseMessageText;
    private final ReplyKeyboard keyboard;
    private final ChatStatus chatStatus;
    @Override
    public PartialBotApiMethod<Message> process(Chat chat) {
        chat.setStatus(chatStatus);

        return SendMessage.builder()
                .text(responseMessageText)
                .replyMarkup(keyboard)
                .chatId(chat.getChatId())
                .build();
    }

    public StartCommand(Role role, String responseMessageText) {
        this.responseMessageText = responseMessageText;
        if (role == Role.ADMIN) {
            chatStatus = ChatStatus.AUTHORIZED_AS_ADMIN;

            KeyboardRow row1 = new KeyboardRow();
            row1.add(MainCommandType.ADMIN_SHOW_PROMOTIONS.getName());
            row1.add(MainCommandType.CREATE_REPORT.getName());

            KeyboardRow row2 = new KeyboardRow();
            row2.add(MainCommandType.ADD_NEW_USER.getName());
            row2.add(MainCommandType.MAKE_AN_ANNOUNCEMENT.getName());

            keyboard = ReplyKeyboardMarkup.builder()
                    .keyboardRow(row1)
                    .keyboardRow(row2)
                    .resizeKeyboard(true)
                    .build();
        } else {
            chatStatus = ChatStatus.AUTHORIZED_AS_USER;

            KeyboardRow row1 = new KeyboardRow();
            row1.add(MainCommandType.USER_SHOW_PROMOTIONS.getName());
            row1.add(MainCommandType.SEND_RECEIPT.getName());

            KeyboardRow row2 = new KeyboardRow();
            row2.add(MainCommandType.BALANCE.getName());

            keyboard = ReplyKeyboardMarkup.builder()
                    .keyboardRow(row1)
                    .keyboardRow(row2)
                    .resizeKeyboard(true)
                    .build();
        }
    }
}