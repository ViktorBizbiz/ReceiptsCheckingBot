package ua.bizbiz.receiptscheckingbot.bot.processor.photo.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu.HomeCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu.StartCommand;
import ua.bizbiz.receiptscheckingbot.bot.processor.photo.PhotoProcessor;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Role;
import ua.bizbiz.receiptscheckingbot.persistance.repository.SubscriptionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;
import ua.bizbiz.receiptscheckingbot.util.DataHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.*;
import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.DATE_TIME_FORMAT;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhotoReceiptProcessor implements PhotoProcessor {

    private final DataHolder dataHolder;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Override
    public List<Validable> process(Chat chat, Message msg, LocalDateTime dateTime) {
        final List<Validable> responses = new ArrayList<>();
        final var fileId = msg.getPhoto().get(0).getFileId();
        final var subscriptionId = dataHolder.getSubscriptionId();
        final var drugsQuantity = msg.getCaption();

        if (drugsQuantity == null) {
            responses.add(new StartCommand(chat, FORGOT_ABOUT_DRUGS_QUANTITY).process(chat));
            log.info("User didn't write quantity of drugs");
            return responses;
        }
        var senderPromotionName = "";
        var senderUserFullName = "";
        final var subscription = subscriptionRepository.findById(Long.parseLong(subscriptionId));
        if (subscription.isPresent()) {
            senderPromotionName = subscription.get().getPromotion().getName();
            senderUserFullName = subscription.get().getUser().getFullName();
        }

        final var caption = String.format(RECEIPT_INFO, senderUserFullName, senderPromotionName, drugsQuantity);

        final var dtf = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        final var nowText = dtf.format(dateTime);

        final List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        final var callbackAccept = subscriptionId + "\n" + ACCEPT + "\n" + dateTime + "\n" + drugsQuantity;
        final var callbackCancel = subscriptionId + "\n" + CANCEL + "\n" + dateTime + "\n" + drugsQuantity;
        buttons.add(getInlineButton(ACCEPT, callbackAccept));
        buttons.add(getInlineButton(CANCEL, callbackCancel));
        final var keyboard = InlineKeyboardMarkup.builder().keyboard(buttons).build();

        userRepository.findAllByRoleAndChatIsNotNull(Role.ADMIN).ifPresent(admins ->
                admins.forEach(admin -> responses.add(SendPhoto.builder()
                        .photo(new InputFile(fileId))
                        .chatId(admin.getChat().getChatId())
                        .caption(caption)
                        .replyMarkup(keyboard)
                        .build())));
        responses.add(SendMessage.builder()
                .text(String.format(PHOTO_IN_PROCESSING, senderPromotionName, drugsQuantity, nowText))
                .chatId(chat.getChatId())
                .build());
        responses.add(new HomeCommand(chat).process(chat));
        log.info("Photo sent in processing");

        dataHolder.setPhotoCreationTime(dateTime);
        // clean DataHolder
        dataHolder.setSubscriptionId(null);
        return responses;
    }

    private List<InlineKeyboardButton> getInlineButton(String text, String callbackData) {
        return List.of(InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build());
    }
}
