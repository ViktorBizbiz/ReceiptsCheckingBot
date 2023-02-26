package ua.bizbiz.receiptscheckingbot.bot.commands.impl;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ua.bizbiz.receiptscheckingbot.bot.commands.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.PromotionCrudCommandType;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.HomeCommandType;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.ChatStatus;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Promotion;

import java.util.List;

public class AdminShowPromotionsCommand implements ProcessableCommand {

    private final String responseMessageText;
    private final ReplyKeyboard keyboard;
    private final ChatStatus chatStatus;
    @Override
    public PartialBotApiMethod<Message> process(Chat chat) {
        chat.setStatus(chatStatus);
        return SendMessage.builder()
                .text(responseMessageText)
                .replyMarkup(keyboard)
                .chatId(chat.getChatId())
                .build();
    }

    public AdminShowPromotionsCommand(List<Promotion> promotions) {
        StringBuilder promotionsList = new StringBuilder();
        for (Promotion promotion : promotions) {
            promotionsList.append(String.format("""
                            %d. %s
                            Мінімальна кількість: %d уп.
                            Бонус за мінімальну кількість: %d грн.
                            Бонус за кожну наступну упаковку: %d грн.
                            
                            """, promotion.getId(), promotion.getName(), promotion.getMinQuantity(),
                            promotion.getCompletionBonus(), promotion.getResaleBonus()));
        }
        responseMessageText = promotionsList.toString();

        chatStatus = ChatStatus.GETTING_PROMOTIONS;

        KeyboardRow row1 = new KeyboardRow();
        row1.add(PromotionCrudCommandType.CREATE_PROMOTION.getName());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(PromotionCrudCommandType.UPDATE_PROMOTION.getName());

        KeyboardRow row3 = new KeyboardRow();
        row3.add(PromotionCrudCommandType.DELETE_PROMOTION.getName());

        KeyboardRow row4 = new KeyboardRow();
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
        responseMessageText = """
                            Ще немає жодної акції.
                            Спочатку додайте хоча б одну.
                            """;

        chatStatus = ChatStatus.GETTING_PROMOTIONS;

        KeyboardRow row1 = new KeyboardRow();
        row1.add(PromotionCrudCommandType.CREATE_PROMOTION.getName());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(HomeCommandType.HOME.getName());

        keyboard = ReplyKeyboardMarkup.builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
                .resizeKeyboard(true)
                .build();
    }
}
