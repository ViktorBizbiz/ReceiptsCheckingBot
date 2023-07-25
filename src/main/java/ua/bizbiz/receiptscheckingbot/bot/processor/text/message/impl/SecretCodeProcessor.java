package ua.bizbiz.receiptscheckingbot.bot.processor.text.message.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu.DefaultStartCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu.StartCommand;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.message.MessageProcessor;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.SUCCESSFUL_AUTHORIZATION;
import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.WRONG_AUTHORIZATION_CODE;

@Slf4j
@RequiredArgsConstructor
public class SecretCodeProcessor implements MessageProcessor {

    private final UserRepository userRepository;

    @Override
    public List<Validable> process(Chat chat, String text) {
        final List<Validable> responses = new ArrayList<>();
        if (!text.matches("\\d+")) { // only digits
            provideAuthenticationFailedMessage(chat, responses);
            return responses;
        }
        final var secretCode = Long.parseLong(text);
        final var optionalUser = userRepository.findBySecretCode(secretCode);
        // if user not exists
        if (optionalUser.isEmpty()) {
            provideAuthenticationFailedMessage(chat, responses);
            return responses;
        }
        final var user = optionalUser.get();
        final var userChat = user.getChat();
        // if user's chat exists, and it's not the same as current chat
        if (userChat != null &&
                !userChat.getChatId().equals(chat.getChatId())) {
            provideAuthenticationFailedMessage(chat, responses);
            return responses;
        }
        user.setChat(chat);
        chat.setUser(user);
        user.setRegisteredAt(LocalDateTime.now());
        userRepository.save(user);

        responses.add(new StartCommand(chat, SUCCESSFUL_AUTHORIZATION).process(chat));

        log.info("Authentication success");
        return responses;
    }

    private void provideAuthenticationFailedMessage(Chat chat, List<Validable> responses) {
        responses.add(new DefaultStartCommand(WRONG_AUTHORIZATION_CODE).process(chat));
        log.info("Authentication not passed");
    }
}
