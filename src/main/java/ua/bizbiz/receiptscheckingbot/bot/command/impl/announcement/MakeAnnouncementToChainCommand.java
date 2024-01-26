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
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.CHAIN_NAME_AND_TEXT_MESSAGE_REQUEST;

public class MakeAnnouncementToChainCommand implements ProcessableCommand {
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

    public MakeAnnouncementToChainCommand(List<User> users) {
        final var chainList = new StringBuilder();
        users.stream()
                .map(User::getPharmacyChain)
                .collect(Collectors.toCollection(TreeSet::new))
                .forEach(chain ->
                        chainList.append(chain)
                                .append("\n"));

        chainList.append(CHAIN_NAME_AND_TEXT_MESSAGE_REQUEST);

        responseMessageText = chainList.toString();

        chatStatus = ChatStatus.SENDING_ANNOUNCEMENT_TO_CHAIN;

        final var row1 = new KeyboardRow();
        row1.add(CommandType.HOME.getName());

        keyboard = ReplyKeyboardMarkup.builder()
                .keyboardRow(row1)
                .resizeKeyboard(true)
                .build();
    }
}
