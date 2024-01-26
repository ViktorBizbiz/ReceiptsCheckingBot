package ua.bizbiz.receiptscheckingbot.bot.processor.callback.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.CommandType;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu.HomeCommand;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Role;
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;

@ExtendWith(MockitoExtension.class)
class HomeProcessorTest {
  
  @InjectMocks
  private HomeProcessor homeProcessor;
  @Test
  void process_shouldReturnExpectedContent() {
    final var chat = Chat.builder()
            .chatId(1L)
            .user(User.builder().role(Role.ADMIN).build())
            .build();
    final var message = new Message();
    message.setMessageId(2);
    final var callbackData = new String[]{CommandType.HOME.getName()};
    final var expected = new HomeCommand(chat).process(chat);
    
    final var result = homeProcessor.process(chat, callbackData, message);
    
    Assertions.assertEquals(expected, result.get(2));
  }
}
