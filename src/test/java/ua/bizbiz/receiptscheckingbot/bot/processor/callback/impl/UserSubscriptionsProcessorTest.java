package ua.bizbiz.receiptscheckingbot.bot.processor.callback.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu.StartCommand;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Promotion;
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;
import ua.bizbiz.receiptscheckingbot.persistance.repository.PromotionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.SubscriptionRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.SOMETHING_WENT_WRONG;
import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.Emoji.CHECK_MARK_EMOJI;
import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.Emoji.POINT_RIGHT_EMOJI;

@ExtendWith(MockitoExtension.class)
class UserSubscriptionsProcessorTest {
  
  @Mock
  private PromotionRepository promotionRepository;
  @Mock
  private SubscriptionRepository subscriptionRepository;
  @InjectMocks
  private UserSubscriptionsProcessor userSubscriptionsProcessor;

  @Test
  void process_shouldReturnExpectedContentAndSaveSubscriptionWhenButtonTextHasPointRightEmoji() {
    final var promotionName = "Promotion_name";
    final var promotionId = 1L;
    final var promotion = Promotion.builder()
            .name(promotionName)
            .build();
    final var chat = Chat.builder()
            .user(User.builder().fullName("name").build())
            .chatId(1L)
            .build();
    final var message = new Message();
    message.setReplyMarkup(InlineKeyboardMarkup.builder()
            .keyboard(List.of(List.of(InlineKeyboardButton.builder()
                    .text(POINT_RIGHT_EMOJI + promotionName)
                    .build())))
            .build());
    message.setMessageId(1);
    final var callbackData = new String[]{"0", String.valueOf(promotionId), promotionName};
    final var keyboard = InlineKeyboardMarkup.builder()
            .keyboard(List.of(List.of(InlineKeyboardButton.builder()
                    .text(CHECK_MARK_EMOJI + promotionName)
                    .build())))
            .build();
    when(promotionRepository.findById(anyLong())).thenReturn(Optional.of(promotion));
    final var expected = EditMessageReplyMarkup.builder()
            .chatId(chat.getChatId())
            .messageId(message.getMessageId())
            .replyMarkup(keyboard)
            .build();
    
    final var result = userSubscriptionsProcessor.process(chat, callbackData, message);
    
    verify(subscriptionRepository).save(any());
    Assertions.assertEquals(expected, result.get(0));
  }
  @Test
  void process_shouldReturnExpectedContentAndDeleteSubscriptionWhenButtonTextHasCheckMarkEmoji() {
    final var promotionName = "Promotion_name";
    final var promotionId = 1L;
    final var promotion = Promotion.builder()
            .name(promotionName)
            .build();
    final var chat = Chat.builder()
            .user(User.builder()
                    .fullName("name")
                    .id(1L)
                    .build())
            .chatId(1L)
            .build();
    final var message = new Message();
    message.setReplyMarkup(InlineKeyboardMarkup.builder()
            .keyboard(List.of(List.of(InlineKeyboardButton.builder()
                    .text(CHECK_MARK_EMOJI + promotionName)
                    .build())))
            .build());
    message.setMessageId(1);
    final var callbackData = new String[]{"0", String.valueOf(promotionId), promotionName};
    final var keyboard = InlineKeyboardMarkup.builder()
            .keyboard(List.of(List.of(InlineKeyboardButton.builder()
                    .text(POINT_RIGHT_EMOJI + promotionName)
                    .build())))
            .build();
    when(promotionRepository.findById(anyLong())).thenReturn(Optional.of(promotion));
    final var expected = EditMessageReplyMarkup.builder()
            .chatId(chat.getChatId())
            .messageId(message.getMessageId())
            .replyMarkup(keyboard)
            .build();
    
    final var result = userSubscriptionsProcessor.process(chat, callbackData, message);
    
    verify(subscriptionRepository).deleteByPromotionIdAndUserId(promotionId, chat.getUser().getId());
    Assertions.assertEquals(expected, result.get(0));
  }
  @Test
  void process_shouldReturnExpectedContentWhenThereIsNoPromotion() {
    final var promotionName = "Promotion_name";
    final var promotionId = 1L;
    final var chat = Chat.builder()
            .user(User.builder()
                    .fullName("name")
                    .id(1L)
                    .build())
            .chatId(1L)
            .build();
    final var message = new Message();
    message.setReplyMarkup(InlineKeyboardMarkup.builder()
            .keyboard(List.of(List.of(InlineKeyboardButton.builder()
                    .text(CHECK_MARK_EMOJI + promotionName)
                    .build())))
            .build());
    message.setMessageId(1);
    final var callbackData = new String[]{"0", String.valueOf(promotionId), promotionName};
    
    when(promotionRepository.findById(anyLong())).thenReturn(Optional.empty());
    final var expected = new StartCommand(chat, SOMETHING_WENT_WRONG).process(chat);
    
    final var result = userSubscriptionsProcessor.process(chat, callbackData, message);
    
    Assertions.assertEquals(expected, result.get(0));
  }
}
