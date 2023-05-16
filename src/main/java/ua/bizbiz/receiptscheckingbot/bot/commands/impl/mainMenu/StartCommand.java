package ua.bizbiz.receiptscheckingbot.bot.commands.impl.mainMenu;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
    public Validable process(Chat chat) {
        chat.setStatus(chatStatus);
        return SendMessage.builder()
                .text(responseMessageText)
                .replyMarkup(keyboard)
                .chatId(chat.getChatId())
                .build();
    }

    public StartCommand(Chat chat, String responseMessageText) {
        this.responseMessageText = responseMessageText;
        final var role = chat.getUser().getRole();
        if (isAdmin(role)) {
            chatStatus = ChatStatus.AUTHORIZED_AS_ADMIN;

            final var row1 = new KeyboardRow();
            row1.add(MainCommandType.ADMIN_SHOW_PROMOTIONS.getName());
            row1.add(MainCommandType.CREATE_REPORT.getName());

            final var row2 = new KeyboardRow();
            row2.add(MainCommandType.ADMIN_SHOW_USERS.getName());
            row2.add(MainCommandType.MAKE_AN_ANNOUNCEMENT.getName());

            final var row3 = new KeyboardRow();
            row3.add(MainCommandType.CHECK_RECEIPTS.getName());

            keyboard = ReplyKeyboardMarkup.builder()
                    .keyboardRow(row1)
                    .keyboardRow(row2)
                    .keyboardRow(row3)
                    .resizeKeyboard(true)
                    .oneTimeKeyboard(true)
                    .build();
        } else {
            chatStatus = ChatStatus.AUTHORIZED_AS_USER;

            final var row1 = new KeyboardRow();
            row1.add(MainCommandType.USER_SHOW_PROMOTIONS.getName());
            row1.add(MainCommandType.SEND_RECEIPT.getName());

            final var row2 = new KeyboardRow();
            row2.add(MainCommandType.BALANCE.getName());

            keyboard = ReplyKeyboardMarkup.builder()
                    .keyboardRow(row1)
                    .keyboardRow(row2)
                    .resizeKeyboard(true)
                    .oneTimeKeyboard(true)
                    .build();
        }
    }

    private boolean isAdmin(Role role) {
        return role == Role.ADMIN;
    }
}
