package ua.bizbiz.receiptscheckingbot.bot.commands.impl;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ua.bizbiz.receiptscheckingbot.bot.commands.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.HomeCommandType;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.ChatStatus;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Subscription;

import java.util.ArrayList;
import java.util.List;

public class SendReceiptCommand implements ProcessableCommand {

    private final String responseMessageText;
    private final InlineKeyboardMarkup inlineKeyboard;
    private final ChatStatus chatStatus;
    @Override
    public Validable process(Chat chat) {
        chat.setStatus(chatStatus);
        return SendMessage.builder()
                .text(responseMessageText)
                .replyMarkup(inlineKeyboard)
                .chatId(chat.getChatId())
                .build();
    }

    public SendReceiptCommand(List<Subscription> subscriptions) {
        responseMessageText = "\uD83D\uDC47\uD83C\uDFFB Оберіть нижче, за якою підпискою ви хочете відправити чек.";

        chatStatus = ChatStatus.SENDING_RECEIPT;

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (Subscription subscription : subscriptions) {
            buttons.add(List.of(InlineKeyboardButton.builder()
                    .text(subscription.getPromotion().getName())
                    .callbackData(subscription.getId().toString())
                    .build()));
        }
        buttons.add(List.of(InlineKeyboardButton.builder()
                .text(HomeCommandType.HOME.getName())
                .callbackData(HomeCommandType.HOME.getName())
                .build()));
        inlineKeyboard = InlineKeyboardMarkup.builder().keyboard(buttons).build();
    }
}
