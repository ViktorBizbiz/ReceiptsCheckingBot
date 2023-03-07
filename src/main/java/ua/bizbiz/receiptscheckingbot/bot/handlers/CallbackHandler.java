package ua.bizbiz.receiptscheckingbot.bot.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.HomeCommandType;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.HomeCommand;
import ua.bizbiz.receiptscheckingbot.persistance.entity.*;
import ua.bizbiz.receiptscheckingbot.persistance.repository.ChatRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.PromotionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.SubscriptionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;
import ua.bizbiz.receiptscheckingbot.util.DataHolder;
import ua.bizbiz.receiptscheckingbot.util.DeleteUtils;
import ua.bizbiz.receiptscheckingbot.util.PhotoMessageData;

import java.time.LocalDateTime;
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
    private final DataHolder dataHolder;
    public List<Validable> handle(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String[] callbackData = callbackQuery.getData().split("\n");
        Message msg = callbackQuery.getMessage();
        int messageId = msg.getMessageId();

        Chat chat = chatRepository.findByChatId(callbackQuery.getMessage().getChatId());

        List<Validable> responses = new ArrayList<>();

        switch (chat.getStatus()) {
            case USER_GETTING_PROMOTIONS -> {

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
            case SENDING_RECEIPT -> responses.addAll(processChosenSubscription(callbackData[0], chat, messageId));
            case CHECKING_RECEIPTS -> responses.addAll(processCheckReceipt(callbackData, chat, messageId));
        }

        chatRepository.save(chat);
        return responses;
    }

    private List<Validable> processCheckReceipt(String[] callbackData, Chat chat, int messageId) {
        List<Validable> responses = new ArrayList<>();

        Long subscriptionId = Long.parseLong(callbackData[0]);
        LocalDateTime photoCreationTime = LocalDateTime.parse(callbackData[2]);
        Optional<Subscription> subscription = subscriptionRepository.findById(subscriptionId);
        if (subscription.isPresent()) {
            String action = callbackData[1];
            Subscription existingSubscription = subscription.get();
            int drugQuantity = Integer.parseInt(callbackData[3]);
            switch (action) {
                case "✅ Підтвердити" -> {
                    int newQuantity = existingSubscription.getCurrentQuantity() + drugQuantity;
                    subscriptionRepository.save(Subscription.builder()
                            .id(existingSubscription.getId())
                            .user(existingSubscription.getUser())
                            .promotion(existingSubscription.getPromotion())
                            .currentQuantity(newQuantity)
                            .build());
                    responses.add(SendMessage.builder()
                            .chatId(existingSubscription.getUser().getChat().getChatId())
                            .text(String.format("✅ Ваш чек [%s, %d шт., станом на %s] було підтверджено.",
                                    existingSubscription.getPromotion().getName(), drugQuantity, photoCreationTime))
                            .build());
                }
                case "❌ Відхилити" -> {
                    responses.add(SendMessage.builder()
                            .chatId(existingSubscription.getUser().getChat().getChatId())
                            .text(String.format("❌ Ваш чек [%s, %d шт., станом на %s] було відхилено.",
                                    existingSubscription.getPromotion().getName(), drugQuantity, photoCreationTime))
                            .build());

                }
            }
        }
        List<PhotoMessageData> photos = dataHolder.getPhotoMessages();
        List<PhotoMessageData> deletedPhotos = new ArrayList<>();

        for (PhotoMessageData photo : photos) {
            if (photo.getCreationTime().isEqual(photoCreationTime)) {
                responses.add(DeleteUtils.deleteMessage(photo.getMessageId(), photo.getChatId()));
                deletedPhotos.add(photo);
            }
        }

        photos.removeAll(deletedPhotos);

        dataHolder.setPhotoMessages(photos);
        return responses;
    }

    private List<Validable> processChosenSubscription(String subscriptionId, Chat chat, int messageId) {
        chat.setStatus(ChatStatus.SENDING_RECEIPT_PHOTO);
        List<Validable> responses = new ArrayList<>();
        dataHolder.setSubscriptionId(subscriptionId);

        KeyboardRow row1 = new KeyboardRow();
        row1.add(HomeCommandType.HOME.getName());

        ReplyKeyboard keyboard = ReplyKeyboardMarkup.builder()
                .keyboardRow(row1)
                .resizeKeyboard(true)
                .build();

        String responseMessageText = """
                Чекаю на ваше фото.
                ‼️ Також допишіть разом із фото кількість препаратів(в штуках), яку ви хочете підтвердити цим фото.
                В іншому випадку вам не зарахується ця кількість препаратів.
                """;

        responses.add(SendMessage.builder()
                .text(responseMessageText)
                .chatId(chat.getChatId())
                .replyMarkup(keyboard)
                .build());

        responses.add(DeleteUtils.deleteMessage(messageId, chat));

        return responses;
    }
}
