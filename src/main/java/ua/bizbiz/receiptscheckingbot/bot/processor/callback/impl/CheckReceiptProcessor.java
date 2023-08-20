package ua.bizbiz.receiptscheckingbot.bot.processor.callback.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.bizbiz.receiptscheckingbot.bot.processor.callback.CallbackProcessor;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.repository.SubscriptionRepository;
import ua.bizbiz.receiptscheckingbot.util.DataHolder;
import ua.bizbiz.receiptscheckingbot.util.DeleteUtils;
import ua.bizbiz.receiptscheckingbot.util.PhotoMessageData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckReceiptProcessor implements CallbackProcessor {

    private final SubscriptionRepository subscriptionRepository;
    private final DataHolder dataHolder;

    @Override
    public List<Validable> process(Chat chat, String[] callbackData, Message msg) {
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
}
