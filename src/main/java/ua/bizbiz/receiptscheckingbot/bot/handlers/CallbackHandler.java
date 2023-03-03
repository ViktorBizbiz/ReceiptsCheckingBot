package ua.bizbiz.receiptscheckingbot.bot.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.HomeCommandType;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.HomeCommand;
import ua.bizbiz.receiptscheckingbot.persistance.entity.*;
import ua.bizbiz.receiptscheckingbot.persistance.repository.ChatRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.PromotionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.SubscriptionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;
import ua.bizbiz.receiptscheckingbot.util.DeleteUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CallbackHandler {

    private final ChatRepository chatRepository;
    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    public List<Validable> handle(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String[] callbackData = callbackQuery.getData().split("\n");

        Chat chat = chatRepository.findByChatId(callbackQuery.getMessage().getChatId());

        List<Validable> responses = new ArrayList<>();

        if (chat.getStatus() == ChatStatus.USER_GETTING_PROMOTIONS) {
            Message msg = callbackQuery.getMessage();
            int messageId = msg.getMessageId();

            if (callbackData[0].equalsIgnoreCase(HomeCommandType.HOME.getName())) {
                responses.addAll(DeleteUtils.deleteMessages(messageId, 1, chat));
                responses.add(new HomeCommand(chat.getUser().getRole()).process(chat));
                chatRepository.save(chat);
                return responses;
            }

            InlineKeyboardMarkup keyboard = msg.getReplyMarkup();
            int buttonId = Integer.parseInt(callbackData[0]);
            var button = keyboard.getKeyboard().get(buttonId);

            Optional<Promotion> promotion = promotionRepository.findById(Long.parseLong(callbackData[1]));
            if (button.get(0).getText().equalsIgnoreCase("\uD83D\uDC49\uD83C\uDFFB " + callbackData[2])) {
                if (promotion.isPresent()) {
                    button.get(0).setText("✅ " + callbackData[2]);
                    responses = List.of(EditMessageReplyMarkup.builder()
                            .replyMarkup(keyboard)
                            .messageId(msg.getMessageId())
                            .chatId(chat.getChatId())
                            .build());
                    User user = chat.getUser();
                    subscriptionRepository.save(Subscription.builder()
                            .currentQuantity(0)
                            .promotion(promotion.get())
                            .user(user)
                            .build());
                }
            } else {
                if (promotion.isPresent()) {
                    button.get(0).setText("\uD83D\uDC49\uD83C\uDFFB " + callbackData[2]);
                    responses = List.of(EditMessageReplyMarkup.builder()
                            .replyMarkup(keyboard)
                            .messageId(msg.getMessageId())
                            .chatId(chat.getChatId())
                            .build());
                    subscriptionRepository.deleteByPromotionId(promotion.get().getId());
                }
            }
        }
        chatRepository.save(chat);
        return responses;
    }
}
