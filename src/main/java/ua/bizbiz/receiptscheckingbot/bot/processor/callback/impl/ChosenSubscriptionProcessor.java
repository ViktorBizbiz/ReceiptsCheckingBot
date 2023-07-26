package ua.bizbiz.receiptscheckingbot.bot.processor.callback.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.CommandType;
import ua.bizbiz.receiptscheckingbot.bot.processor.callback.CallbackProcessor;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.ChatStatus;
import ua.bizbiz.receiptscheckingbot.util.DataHolder;
import ua.bizbiz.receiptscheckingbot.util.DeleteUtils;

import java.util.ArrayList;
import java.util.List;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.WAITING_FOR_PHOTO;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChosenSubscriptionProcessor implements CallbackProcessor {

    private final DataHolder dataHolder;

    @Override
    public List<Validable> process(Chat chat, String[] callbackData, Message msg) {
        chat.setStatus(ChatStatus.SENDING_RECEIPT_PHOTO);
        final var subscriptionId = callbackData[0];
        final var messageId = msg.getMessageId();
        final List<Validable> responses = new ArrayList<>();
        dataHolder.setSubscriptionId(subscriptionId);
        log.info("User chose subscription with ID: " + subscriptionId);
        final var row1 = new KeyboardRow();
        row1.add(CommandType.HOME.getName());

        final var keyboard = ReplyKeyboardMarkup.builder()
                .keyboardRow(row1)
                .resizeKeyboard(true)
                .build();

        responses.add(SendMessage.builder()
                .text(WAITING_FOR_PHOTO)
                .chatId(chat.getChatId())
                .replyMarkup(keyboard)
                .build());

        responses.add(DeleteUtils.deleteMessage(messageId, chat));

        return responses;
    }
}
