package ua.bizbiz.receiptscheckingbot.bot.commands.impl;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ua.bizbiz.receiptscheckingbot.bot.commands.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Subscription;

import java.util.List;

public class BalanceCommand implements ProcessableCommand {

    private final String responseMessageText;
    @Override
    public Validable process(Chat chat) {
        return SendMessage.builder()
                .text(responseMessageText)
                .chatId(chat.getChatId())
                .build();
    }

    public BalanceCommand(List<Subscription> subscriptions) {
        StringBuilder response = new StringBuilder();
        int totalBonus = 0;
        int subscriptionBonus = 0;
        response.append("За вашими підписками у вас наступні результати:\n\n");
        for (Subscription sub : subscriptions) {
            var promotionName = sub.getPromotion().getName();
            var currentQuantity = sub.getCurrentQuantity();
            var minQuantity = sub.getPromotion().getMinQuantity();
            var resaleBonus = sub.getPromotion().getResaleBonus();

            if (currentQuantity >= minQuantity)
                subscriptionBonus = currentQuantity * resaleBonus;

            response.append(String.format("""
                    %s
                    Теперішня кількість/Мінімальна кількість: %s/%s
                    Загальний бонус за підпискою: %s
                    
                    """, promotionName, currentQuantity, minQuantity, subscriptionBonus));

            totalBonus += subscriptionBonus;
        }
        response.append("Усього бонусів: ").append(totalBonus);

        responseMessageText = response.toString();
    }
}
