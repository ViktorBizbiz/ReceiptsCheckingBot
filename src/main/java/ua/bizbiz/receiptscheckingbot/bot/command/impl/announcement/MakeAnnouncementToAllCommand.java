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

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.TEXT_MESSAGE_REQUEST;

public class MakeAnnouncementToAllCommand implements ProcessableCommand {
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

    public MakeAnnouncementToAllCommand() {
        responseMessageText = TEXT_MESSAGE_REQUEST;

        chatStatus = ChatStatus.SENDING_ANNOUNCEMENT_TO_ALL;

        final var row1 = new KeyboardRow();
        row1.add(CommandType.HOME.getName());

        keyboard = ReplyKeyboardMarkup.builder()
                .keyboardRow(row1)
                .resizeKeyboard(true)
                .build();
    }
}
