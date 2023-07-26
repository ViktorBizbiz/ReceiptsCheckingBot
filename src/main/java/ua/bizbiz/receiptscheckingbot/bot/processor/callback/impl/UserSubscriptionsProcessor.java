package ua.bizbiz.receiptscheckingbot.bot.processor.callback.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu.StartCommand;
import ua.bizbiz.receiptscheckingbot.bot.processor.callback.CallbackProcessor;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Subscription;
import ua.bizbiz.receiptscheckingbot.persistance.repository.PromotionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.SubscriptionRepository;

import java.util.ArrayList;
import java.util.List;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.SOMETHING_WENT_WRONG;
import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.Emoji.CHECK_MARK_EMOJI;
import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.Emoji.POINT_RIGHT_EMOJI;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSubscriptionsProcessor implements CallbackProcessor {

    private final PromotionRepository promotionRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public List<Validable> process(Chat chat, String[] callbackData, Message msg) {
        final List<Validable> responses = new ArrayList<>();
        final var keyboard = msg.getReplyMarkup();
        final var buttonId = Integer.parseInt(callbackData[0]);
        final var button = keyboard.getKeyboard().get(buttonId).get(0);
        final var promotionId = Long.parseLong(callbackData[1]);
        final var promotionName = callbackData[2];

        final var promotion = promotionRepository.findById(promotionId);
        if (promotion.isEmpty()) {
            responses.add(new StartCommand(chat, SOMETHING_WENT_WRONG).process(chat));
            return responses;
        }
        if (!button.getText().equals(POINT_RIGHT_EMOJI + promotionName)) {
            button.setText(POINT_RIGHT_EMOJI + promotionName);
            responses.add(EditMessageReplyMarkup.builder()
                    .replyMarkup(keyboard)
                    .messageId(msg.getMessageId())
                    .chatId(chat.getChatId())
                    .build());
            subscriptionRepository.deleteByPromotionIdAndUserId(promotionId, chat.getUser().getId());
            log.info("User [" + chat.getUser().getFullName() + "] unsubscribed on promotion");
            return responses;
        }
        button.setText(CHECK_MARK_EMOJI + promotionName);
        responses.add(EditMessageReplyMarkup.builder()
                .replyMarkup(keyboard)
                .messageId(msg.getMessageId())
                .chatId(chat.getChatId())
                .build());
        subscriptionRepository.save(Subscription.builder()
                .currentQuantity(0)
                .currentBonus(0)
                .promotion(promotion.get())
                .user(chat.getUser())
                .build());
        log.info("User [" + chat.getUser().getFullName() + "] subscribed on promotion");
        return responses;
    }
}
