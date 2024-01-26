package ua.bizbiz.receiptscheckingbot.bot.processor.photo.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu.HomeCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu.StartCommand;
import ua.bizbiz.receiptscheckingbot.persistance.entity.*;
import ua.bizbiz.receiptscheckingbot.persistance.repository.SubscriptionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;
import ua.bizbiz.receiptscheckingbot.util.DataHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.*;
import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.DATE_TIME_FORMAT;

@ExtendWith(MockitoExtension.class)
class PhotoReceiptProcessorTest {
  
  @Mock
  private DataHolder dataHolder;
  @Mock
  private SubscriptionRepository subscriptionRepository;
  @Mock
  private UserRepository userRepository;
  @InjectMocks
  private PhotoReceiptProcessor photoReceiptProcessor;

  @Test
  void process_returnExpectedContentWhenGivenPhotoCaption() {
    final var chat = Chat.builder()
            .user(User.builder().role(Role.USER).build())
            .chatId(5L)
            .build();
    final var photo = new PhotoSize();
    photo.setFileId("1");
    final var message = new Message();
    message.setPhoto(List.of(photo));
    message.setCaption("3");
    final var subscription = Subscription.builder()
            .user(User.builder().fullName("user_name").build())
            .promotion(Promotion.builder().name("promotion_name").build())
            .build();
    when(dataHolder.getSubscriptionId()).thenReturn("1");
    when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.of(subscription));
    when(userRepository.findAllByRoleAndChatIsNotNull(Role.ADMIN))
            .thenReturn(Optional.of(List.of(User.builder()
                    .chat(Chat.builder().chatId(1L).build())
                    .build())));
    final var dtf = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    final var now = LocalDateTime.now();
    final var nowText = dtf.format(now);
    final List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
    final var callbackAccept = "1" + "\n" + ACCEPT + "\n" + now + "\n" + "3";
    final var callbackCancel = "1" + "\n" + CANCEL + "\n" + now + "\n" + "3";
    buttons.add(getInlineButton(ACCEPT, callbackAccept));
    buttons.add(getInlineButton(CANCEL, callbackCancel));
    final var keyboard = InlineKeyboardMarkup.builder().keyboard(buttons).build();
    final var photoMessageForAdmin = SendPhoto.builder()
            .photo(new InputFile("1"))
            .chatId(1L)
            .caption("""
                Від: user_name
                Назва акції: promotion_name
                Кількість препарату для підтвердження: 3 шт.
                
                ‼️ Зверніть увагу!
                Щоб підтвердити/відхилити чек, перейдіть у режим "Перевірка чеків 🔍" у головному меню.
                """)
            .replyMarkup(keyboard)
            .build();
    final var infoMessageForUser = SendMessage.builder()
            .text(String.format(PHOTO_IN_PROCESSING, "promotion_name", "3", nowText))
            .chatId(5L)
            .build();
    final var homeMessageForUser = new HomeCommand(chat).process(chat);
    
    final var expected = List.of(photoMessageForAdmin, infoMessageForUser, homeMessageForUser);
    
    final var result = photoReceiptProcessor.process(chat, message, now);
    
    Assertions.assertEquals(expected, result);
  }
  @Test
  void process_returnStartMessageWhenNotGivenPhotoCaption() {
    final var chat = Chat.builder()
            .user(User.builder().role(Role.USER).build())
            .chatId(5L)
            .build();
    final var photo = new PhotoSize();
    photo.setFileId("1");
    final var message = new Message();
    message.setPhoto(List.of(photo));
    when(dataHolder.getSubscriptionId()).thenReturn("1");
    final var now = LocalDateTime.now();
    final var expected = new StartCommand(chat, FORGOT_ABOUT_DRUGS_QUANTITY).process(chat);
    
    final var result = photoReceiptProcessor.process(chat, message, now);
    
    Assertions.assertEquals(expected, result.get(0));
  }
  
  private List<InlineKeyboardButton> getInlineButton(String text, String callbackData) {
    return List.of(InlineKeyboardButton.builder()
            .text(text)
            .callbackData(callbackData)
            .build());
  }
}
