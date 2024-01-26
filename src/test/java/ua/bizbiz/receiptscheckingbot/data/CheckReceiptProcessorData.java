package ua.bizbiz.receiptscheckingbot.data;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Promotion;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Subscription;
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;
import ua.bizbiz.receiptscheckingbot.util.PhotoMessageData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class CheckReceiptProcessorData {
  
  private static final long ID = 1L;
  private static final LocalDateTime DATE_TIME = LocalDateTime.now();
  private static final String DRUGS_QUANTITY = "3";
  
  public Subscription getSubscription() {
    final var promotion = Promotion.builder()
            .minQuantity(3)
            .resaleBonus(50)
            .build();
    final var chat = Chat.builder()
            .chatId(ID)
            .build();
    final var user = User.builder()
            .chat(chat)
            .build();
    chat.setUser(user);
    return Subscription.builder()
            .user(user)
            .promotion(promotion)
            .currentQuantity(2)
            .build();
  }
  
  public String[] getCallbackData(String action) {
    return new String[]{String.valueOf(ID), action, DATE_TIME.toString(), DRUGS_QUANTITY};
  }
  
  public List<PhotoMessageData> getPhotos() {
    List<PhotoMessageData> photos = new ArrayList<>();
    for (int i = 1; i < 3; i++) {
      photos.add(PhotoMessageData.builder()
              .messageId(1)
              .chatId((long) i)
              .creationTime(DATE_TIME)
              .build());
    }
    return photos;
  }
  
  public SendMessage getExpectedSendMessage(String message) {
    final var dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    return SendMessage.builder()
            .chatId(ID)
            .text(String.format(message,
                    getSubscription().getPromotion().getName(), DRUGS_QUANTITY,
                    dtf.format(DATE_TIME)))
            .build();
  }
}
