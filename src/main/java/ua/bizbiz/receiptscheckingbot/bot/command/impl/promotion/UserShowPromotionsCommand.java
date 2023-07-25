package ua.bizbiz.receiptscheckingbot.bot.command.impl.promotion;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ua.bizbiz.receiptscheckingbot.bot.command.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.CommandType;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.ChatStatus;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Promotion;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.*;
import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.Emoji.*;

public class UserShowPromotionsCommand implements ProcessableCommand {

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

    public UserShowPromotionsCommand(List<Promotion> promotions, List<Subscription> userSubscriptions) {
        final var promotionsList = new StringBuilder();
        for (Promotion promotion : promotions) {
            promotionsList.append(String.format(PROMOTION_INFO_2, promotion.getName(), promotion.getMinQuantity(),
                    promotion.getCompletionBonus(), promotion.getResaleBonus()));
        }
        promotionsList.append(POINT_DOWN_EMOJI + CHOOSE_PROMOTION);
        promotionsList.append(LOSE_PROGRESS_WARNING_ON_UNSUBSCRIBE);
        responseMessageText = promotionsList.toString();

        chatStatus = ChatStatus.USER_GETTING_PROMOTIONS;

        final List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        var buttonId = 0;
        for (Promotion promotion : promotions) {
            var buttonName = POINT_RIGHT_EMOJI + promotion.getName();

            if (userSubscriptions.stream()
                    .anyMatch(sub -> Objects.equals(sub.getPromotion().getId(), promotion.getId())))
                buttonName = CHECK_MARK_EMOJI + promotion.getName();

            buttons.add(List.of(InlineKeyboardButton.builder()
                    .text(buttonName)
                    .callbackData(buttonId + "\n" + promotion.getId().toString() + "\n" + promotion.getName())
                    .build()));
            buttonId++;
        }
        buttons.add(List.of(InlineKeyboardButton.builder()
                .text(CommandType.HOME.getName())
                .callbackData(CommandType.HOME.getName())
                .build()));
        keyboard = InlineKeyboardMarkup.builder()
                .keyboard(buttons)
                .build();
    }
}
