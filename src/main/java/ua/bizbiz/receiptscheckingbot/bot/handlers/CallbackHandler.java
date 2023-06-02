package ua.bizbiz.receiptscheckingbot.bot.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.HomeCommandType;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.mainmenu.HomeCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.mainmenu.StartCommand;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.ChatStatus;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Subscription;
import ua.bizbiz.receiptscheckingbot.persistance.repository.ChatRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.PromotionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.SubscriptionRepository;
import ua.bizbiz.receiptscheckingbot.util.DataHolder;
import ua.bizbiz.receiptscheckingbot.util.DeleteUtils;
import ua.bizbiz.receiptscheckingbot.util.PhotoMessageData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.*;
import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.Emoji.CHECK_MARK_EMOJI;
import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.Emoji.POINT_RIGHT_EMOJI;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallbackHandler {

    private final ChatRepository chatRepository;
    private final PromotionRepository promotionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final DataHolder dataHolder;
    public List<Validable> handle(Update update) {
        final var callbackQuery = update.getCallbackQuery();
        final var callbackData = callbackQuery.getData().split("\n");
        final var message = callbackQuery.getMessage();
        final var messageId = message.getMessageId();
        final var chat = chatRepository.findByChatId(callbackQuery.getMessage().getChatId());

        final List<Validable> responses = tryProcessHomeCommand(callbackData, messageId, chat);
        if (!responses.isEmpty())
            return responses;

        log.info("Update handling with status: " + chat.getStatus());
        switch (chat.getStatus()) {
            case USER_GETTING_PROMOTIONS -> responses.addAll(processUserSubscriptions(callbackData, message, chat));
            case SENDING_RECEIPT -> responses.addAll(processChosenSubscription(callbackData[0], chat, messageId));
            case CHECKING_RECEIPTS -> responses.addAll(processCheckReceipt(callbackData, chat));
        }

        chatRepository.save(chat);
        return responses;
    }

    private List<Validable> tryProcessHomeCommand(String[] callbackData, int messageId, Chat chat) {
        final List<Validable> responses = new ArrayList<>();
        final var text = callbackData[0];
        if (text.equalsIgnoreCase(HomeCommandType.HOME.getName())) {
            log.info("HomeCommandType detected: " + HomeCommandType.HOME);
            responses.addAll(DeleteUtils.deleteMessages(messageId, 1, chat));
            responses.add(new HomeCommand(chat).process(chat));
            chatRepository.save(chat);
        }
        return responses;
    }

    private List<Validable> processUserSubscriptions(String[] callbackData, Message msg, Chat chat) {
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

    private List<Validable> processCheckReceipt(String[] callbackData, Chat chat) {
        final List<Validable> responses = new ArrayList<>();

        final var subscriptionId = Long.parseLong(callbackData[0]);
        final var dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        final var photoCreationTime = LocalDateTime.parse(callbackData[2]);
        final var photoCreationTimeText = dtf.format(photoCreationTime);
        final var optionalSubscription = subscriptionRepository.findById(subscriptionId);
        if (optionalSubscription.isPresent()) {
            final var action = callbackData[1];
            final var subscription = optionalSubscription.get();
            final var drugQuantity = Integer.parseInt(callbackData[3]);
            log.info("Admin [" + chat.getUser().getFullName() + "] did next action: " + action);
            switch (action) {
                case ACCEPT -> {
                    final var newQuantity = subscription.getCurrentQuantity() + drugQuantity;
                    final var minQuantity = subscription.getPromotion().getMinQuantity();
                    final var resaleBonus = subscription.getPromotion().getResaleBonus();
                    var subscriptionBonus = 0;

                    if (newQuantity >= minQuantity)
                        subscriptionBonus = newQuantity * resaleBonus;

                    subscription.setCurrentQuantity(newQuantity);
                    subscription.setCurrentBonus(subscriptionBonus);
                    subscriptionRepository.save(subscription);

                    responses.add(SendMessage.builder()
                            .chatId(subscription.getUser().getChat().getChatId())
                            .text(String.format(RECEIPT_ACCEPTED,
                                    subscription.getPromotion().getName(), drugQuantity, photoCreationTimeText))
                            .build());
                }
                case CANCEL ->
                    responses.add(SendMessage.builder()
                            .chatId(subscription.getUser().getChat().getChatId())
                            .text(String.format(RECEIPT_DECLINED,
                                    subscription.getPromotion().getName(), drugQuantity, photoCreationTimeText))
                            .build());
            }
        }
        final var photos = dataHolder.getPhotoMessages();
        final List<PhotoMessageData> deletedPhotos = new ArrayList<>();

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
        final List<Validable> responses = new ArrayList<>();
        dataHolder.setSubscriptionId(subscriptionId);
        log.info("User chose subscription with ID: " + subscriptionId);
        final var row1 = new KeyboardRow();
        row1.add(HomeCommandType.HOME.getName());

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
