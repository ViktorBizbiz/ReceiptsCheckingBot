package ua.bizbiz.receiptscheckingbot.bot.command.impl.announcement;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ua.bizbiz.receiptscheckingbot.bot.command.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.CommandType;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.ChatStatus;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.WHOM_SEND_MESSAGE;

public class MakeAnnouncementCommand implements ProcessableCommand {

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

    public MakeAnnouncementCommand() {
        responseMessageText = WHOM_SEND_MESSAGE;

        chatStatus = ChatStatus.SENDING_ANNOUNCEMENT;

        final var row1 = new KeyboardRow();
        final var row2 = new KeyboardRow();
        final var row3 = new KeyboardRow();
        final var row4 = new KeyboardRow();

        row1.add(CommandType.TO_ALL.getName());
        row2.add(CommandType.TO_CHAIN.getName());
        row3.add(CommandType.TO_PERSON.getName());
        row4.add(CommandType.HOME.getName());

        keyboard = ReplyKeyboardMarkup.builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
                .keyboardRow(row3)
                .keyboardRow(row4)
                .resizeKeyboard(true)
                .build();
    }
}
