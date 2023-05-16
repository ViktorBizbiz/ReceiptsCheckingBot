package ua.bizbiz.receiptscheckingbot.bot.commands.impl;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ua.bizbiz.receiptscheckingbot.bot.commands.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.HomeCommandType;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.ChatStatus;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Subscription;

import java.util.ArrayList;
import java.util.List;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.CHOOSE_SUBSCRIPTION_TO_SEND_RECEIPT;

public class SendReceiptCommand implements ProcessableCommand {

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

    public SendReceiptCommand(List<Subscription> subscriptions) {
        responseMessageText = CHOOSE_SUBSCRIPTION_TO_SEND_RECEIPT;

        chatStatus = ChatStatus.SENDING_RECEIPT;

        final List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
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
        keyboard = InlineKeyboardMarkup.builder().keyboard(buttons).build();
    }
}
