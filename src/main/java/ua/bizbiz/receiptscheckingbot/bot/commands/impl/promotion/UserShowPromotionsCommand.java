package ua.bizbiz.receiptscheckingbot.bot.commands.impl.promotion;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ua.bizbiz.receiptscheckingbot.bot.commands.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.HomeCommandType;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.ChatStatus;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Promotion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public UserShowPromotionsCommand(List<Promotion> promotions, Chat chat) {
        StringBuilder promotionsList = new StringBuilder();
        for (Promotion promotion : promotions) {
            promotionsList.append(String.format("""
                            %s
                            Мінімальна кількість: %d уп.
                            Бонус за мінімальну кількість: %d грн.
                            Бонус за кожну наступну упаковку: %d грн.

                            """, promotion.getName(), promotion.getMinQuantity(),
                    promotion.getCompletionBonus(), promotion.getResaleBonus()));
        }
        promotionsList.append("\uD83D\uDC47\uD83C\uDFFB Оберіть нижче, на яку акцію ви хочете підписатися/відписатися.\n\n");
        promotionsList.append("❗️❗️❗️ Зверніть увагу! Якщо ви відпишетеся від акції, то втратите увесь прогрес по ній.");
        responseMessageText = promotionsList.toString();

        chatStatus = ChatStatus.USER_GETTING_PROMOTIONS;

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        int i = 0;
        for (Promotion promotion : promotions) {
            String buttonName = "\uD83D\uDC49\uD83C\uDFFB " + promotion.getName();

            if (chat.getUser().getSubscriptions().stream()
                    .anyMatch(sub -> Objects.equals(sub.getPromotion().getId(), promotion.getId())))
                buttonName = "✅ " + promotion.getName();

            buttons.add(List.of(InlineKeyboardButton.builder()
                    .text(buttonName)
                    .callbackData(i + "\n" + promotion.getId().toString() + "\n" + promotion.getName())
                    .build()));
            i++;
        }
        buttons.add(List.of(InlineKeyboardButton.builder()
                .text(HomeCommandType.HOME.getName())
                .callbackData(HomeCommandType.HOME.getName())
                .build()));
        keyboard = InlineKeyboardMarkup.builder().keyboard(buttons).build();
    }
}
