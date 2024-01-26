package ua.bizbiz.receiptscheckingbot.bot.processor.callback.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.CommandType;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.util.DataHolder;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.WAITING_FOR_PHOTO;

@ExtendWith(MockitoExtension.class)
class ChosenSubscriptionProcessorTest {
  
  @Mock
  private DataHolder dataHolder;
  @InjectMocks
  private ChosenSubscriptionProcessor chosenSubscriptionProcessor;

  @Test
  void process_shouldReturnExpectedContent() {
    final var chat = Chat.builder()
            .chatId(1L)
            .build();
    final var message = new Message();
    message.setMessageId(1);
    final var callbackData = new String[]{"1"};
    final var row1 = new KeyboardRow();
    row1.add(CommandType.HOME.getName());
    final var keyboard = ReplyKeyboardMarkup.builder()
            .keyboardRow(row1)
            .resizeKeyboard(true)
            .build();
    final var expected = SendMessage.builder()
            .text(WAITING_FOR_PHOTO)
            .chatId(chat.getChatId())
            .replyMarkup(keyboard)
            .build();
    
    final var result = chosenSubscriptionProcessor.process(chat, callbackData, message);
    
    Assertions.assertEquals(expected, result.get(0));
  }
}
