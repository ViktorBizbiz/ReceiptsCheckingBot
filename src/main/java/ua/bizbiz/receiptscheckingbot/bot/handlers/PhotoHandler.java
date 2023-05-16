package ua.bizbiz.receiptscheckingbot.bot.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.mainMenu.HomeCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.mainMenu.StartCommand;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Role;
import ua.bizbiz.receiptscheckingbot.persistance.repository.ChatRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.SubscriptionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;
import ua.bizbiz.receiptscheckingbot.util.DataHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.*;

@Component
@RequiredArgsConstructor
public class PhotoHandler {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final DataHolder dataHolder;

    public List<Validable> handle(Update update) {
        final var chat = chatRepository.findByChatId(update.getMessage().getChatId());
        final List<Validable> responses = new ArrayList<>();

        switch (chat.getStatus()) {
            case SENDING_RECEIPT_PHOTO -> responses.addAll(processPhotoReceipt(update.getMessage(), chat));
        }
        chatRepository.save(chat);
        return responses;
    }

    private List<Validable> processPhotoReceipt(Message message, Chat chat) {
        final List<Validable> responses = new ArrayList<>();
        final var fileId = message.getPhoto().get(0).getFileId();
        final var subscriptionId = dataHolder.getSubscriptionId();
        final var drugsQuantity = message.getCaption();

        if (drugsQuantity == null) {
            responses.add(new StartCommand(chat, FORGOT_ABOUT_DRUGS_QUANTITY).process(chat));
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

        final var dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        final var now = LocalDateTime.now();
        final var nowText = dtf.format(now);

        final List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        final var callbackAccept = subscriptionId + "\n" + ACCEPT + "\n" + now + "\n" + drugsQuantity;
        final var callbackCancel = subscriptionId + "\n" + CANCEL + "\n" + now + "\n" + drugsQuantity;
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

        dataHolder.setPhotoCreationTime(now);
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
