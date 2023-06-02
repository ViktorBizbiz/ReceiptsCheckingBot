package ua.bizbiz.receiptscheckingbot.bot.commands.impl;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ua.bizbiz.receiptscheckingbot.bot.commands.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Subscription;

import java.util.List;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.*;

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
        final var response = new StringBuilder();
        var totalBonus = 0;
        response.append(SUBSCRIPTIONS_RESULTS);
        for (Subscription sub : subscriptions) {
            final var promotionName = sub.getPromotion().getName();
            final var currentQuantity = sub.getCurrentQuantity();
            final var subscriptionBonus = sub.getCurrentBonus();
            final var minQuantity = sub.getPromotion().getMinQuantity();

            response.append(String.format(YOUR_BALANCE_INFO,
                    promotionName, currentQuantity, minQuantity, subscriptionBonus));

            totalBonus += subscriptionBonus;
        }
        response.append(TOTAL_BALANCE).append(totalBonus);

        responseMessageText = response.toString();
    }
}
