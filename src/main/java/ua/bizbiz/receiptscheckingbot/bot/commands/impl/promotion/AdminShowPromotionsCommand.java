package ua.bizbiz.receiptscheckingbot.bot.commands.impl.promotion;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ua.bizbiz.receiptscheckingbot.bot.commands.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.HomeCommandType;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.crud.PromotionCrudCommandType;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.ChatStatus;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Promotion;

import java.util.List;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.NO_PROMOTION_FOUND_2;
import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.PROMOTION_INFO;

public class AdminShowPromotionsCommand implements ProcessableCommand {

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

    public AdminShowPromotionsCommand(List<Promotion> promotions) {
        final var promotionsList = new StringBuilder();
        for (Promotion promotion : promotions) {
            promotionsList.append(String.format(PROMOTION_INFO,
                    promotion.getId(), promotion.getName(), promotion.getMinQuantity(),
                    promotion.getCompletionBonus(), promotion.getResaleBonus()));
        }
        responseMessageText = promotionsList.toString();

        chatStatus = ChatStatus.ADMIN_GETTING_PROMOTIONS;

        final var row1 = new KeyboardRow();
        final var row2 = new KeyboardRow();
        final var row3 = new KeyboardRow();
        final var row4 = new KeyboardRow();

        row1.add(PromotionCrudCommandType.CREATE_PROMOTION.getName());
        row2.add(PromotionCrudCommandType.UPDATE_PROMOTION.getName());
        row3.add(PromotionCrudCommandType.DELETE_PROMOTION.getName());
        row4.add(HomeCommandType.HOME.getName());

        keyboard = ReplyKeyboardMarkup.builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
                .keyboardRow(row3)
                .keyboardRow(row4)
                .resizeKeyboard(true)
                .build();
    }

    public AdminShowPromotionsCommand() {
        responseMessageText = NO_PROMOTION_FOUND_2;

        chatStatus = ChatStatus.ADMIN_GETTING_PROMOTIONS;

        final var row1 = new KeyboardRow();
        final var row2 = new KeyboardRow();

        row1.add(PromotionCrudCommandType.CREATE_PROMOTION.getName());
        row2.add(HomeCommandType.HOME.getName());

        keyboard = ReplyKeyboardMarkup.builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
                .resizeKeyboard(true)
                .build();
    }
}
