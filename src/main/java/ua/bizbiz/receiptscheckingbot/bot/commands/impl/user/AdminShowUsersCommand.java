package ua.bizbiz.receiptscheckingbot.bot.commands.impl.user;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ua.bizbiz.receiptscheckingbot.bot.commands.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.HomeCommandType;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.crud.UserCrudCommandType;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.ChatStatus;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Role;
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;

import java.util.List;

public class AdminShowUsersCommand implements ProcessableCommand {

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

    public AdminShowUsersCommand(List<User> users) {
        StringBuilder usersList = new StringBuilder();
        for (User user : users) {
            usersList.append(String.format("%d. %s", user.getId(), user.getFullName()))
                    .append(detectUserAdditionalInfo(user))
                    .append("\n");
        }
        responseMessageText = usersList.toString();

        chatStatus = ChatStatus.ADMIN_GETTING_USERS;

        final var row1 = new KeyboardRow();
        row1.add(UserCrudCommandType.CREATE_USER.getName());
        row1.add(UserCrudCommandType.UPDATE_USER.getName());

        final var row2 = new KeyboardRow();
        row2.add(UserCrudCommandType.DELETE_USER.getName());
        row2.add(UserCrudCommandType.READ_USER.getName());

        final var row3 = new KeyboardRow();
        row3.add(HomeCommandType.HOME.getName());

        keyboard = ReplyKeyboardMarkup.builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
                .keyboardRow(row3)
                .resizeKeyboard(true)
                .build();
    }

    private String detectUserAdditionalInfo(User user) {
        if (user.getChat() == null) {
            return " (Не активовано)";
        }
        if (user.getRole() == Role.ADMIN) {
            return " (Адмін)";
        }
        return "";
    }
}
