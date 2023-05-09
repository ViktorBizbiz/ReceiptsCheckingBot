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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.mainMenu.HomeCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.mainMenu.StartCommand;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Role;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Subscription;
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;
import ua.bizbiz.receiptscheckingbot.persistance.repository.ChatRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.SubscriptionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;
import ua.bizbiz.receiptscheckingbot.util.ClientAnswerMessages;
import ua.bizbiz.receiptscheckingbot.util.DataHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PhotoHandler {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final DataHolder dataHolder;

    public List<Validable> handle(Update update) {
        Chat chat = chatRepository.findByChatId(update.getMessage().getChatId());

        List<Validable> responses = new ArrayList<>();

        switch (chat.getStatus()) {
            case SENDING_RECEIPT_PHOTO -> responses.addAll(processPhotoReceipt(update.getMessage(), chat));
        }

        chatRepository.save(chat);
        return responses;
    }

    private List<Validable> processPhotoReceipt(Message message, Chat chat) {
        List<Validable> responses = new ArrayList<>();
        String fileId = message.getPhoto().get(0).getFileId();
        String subscriptionId = dataHolder.getSubscriptionId();

        String drugsQuantity = message.getCaption();
        if (drugsQuantity == null) {
            responses.add(new StartCommand(chat.getUser().getRole(),
                    ClientAnswerMessages.FORGOT_ABOUT_DRUGS_QUANTITY).process(chat));
            return responses;
        }
        String senderPromotionName = "";
        String senderUserFullName = "";
        Optional<Subscription> subscription = subscriptionRepository.findById(Long.parseLong(subscriptionId));
        if (subscription.isPresent()) {
            senderPromotionName = subscription.get().getPromotion().getName();
            senderUserFullName = subscription.get().getUser().getFullName();
        }

        String caption = String.format(ClientAnswerMessages.RECEIPT_INFO,
                senderUserFullName, senderPromotionName, drugsQuantity);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String nowText = dtf.format(now);

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        String callbackAccept = subscriptionId + "\n✅ Підтвердити\n" + now + "\n" + drugsQuantity;
        String callbackCancel = subscriptionId + "\n❌ Відхилити\n" + now + "\n" + drugsQuantity;
        buttons.add(getInlineButton("✅ Підтвердити", callbackAccept));
        buttons.add(getInlineButton("❌ Відхилити", callbackCancel));
        ReplyKeyboard keyboard = InlineKeyboardMarkup.builder().keyboard(buttons).build();

        Optional<List<User>> admins = userRepository.findAllByRoleAndChatIsNotNull(Role.ADMIN);
        if (admins.isPresent() && admins.get().size() != 0) {
            for (User admin : admins.get()) {
                responses.add(SendPhoto.builder()
                        .photo(new InputFile(fileId))
                        .chatId(admin.getChat().getChatId())
                        .caption(caption)
                        .replyMarkup(keyboard)
                        .build());
            }
        }
        responses.add(SendMessage.builder()
                .text(String.format(ClientAnswerMessages.PHOTO_IN_PROCESSING,
                        senderPromotionName, drugsQuantity, nowText))
                .chatId(chat.getChatId())
                .build());
        responses.add(new HomeCommand(chat.getUser().getRole()).process(chat));

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
