package ua.bizbiz.receiptscheckingbot.bot.processor.text.message.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu.StartCommand;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.message.MessageProcessor;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Role;
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.*;

@Component
@RequiredArgsConstructor
public class AnnouncementProcessor implements MessageProcessor {

    private final UserRepository userRepository;

    @Override
    public List<Validable> process(Chat chat, String text) {
        final List<Validable> responses = new ArrayList<>();
        final var users = userRepository.findAllByRoleAndChatIsNotNull(Role.USER);
        switch (chat.getStatus()) {
            case SENDING_ANNOUNCEMENT_TO_ALL -> {
                if (users.isPresent() && !users.get().isEmpty()) {
                    for (User user : users.get())
                        responses.add(getSendMessageWithSender(text, chat, user));
                    responses.add(new StartCommand(chat, MESSAGE_SENT_SUCCESSFULLY).process(chat));
                } else {
                    responses.add(new StartCommand(chat, NO_AUTHORIZED_USER_FOUND).process(chat));
                }
            }
            case SENDING_ANNOUNCEMENT_TO_PERSON -> {
                final var userId = Long.parseLong(text.substring(0, text.indexOf("\n")));
                text = text.substring(text.indexOf("\n"));
                final var user = userRepository.findById(userId);
                if (user.isPresent()) {
                    responses.add(getSendMessageWithSender(text, chat, user.get()));
                    responses.add(new StartCommand(chat, MESSAGE_SENT_SUCCESSFULLY).process(chat));
                } else {
                    responses.add(new StartCommand(chat, NO_USER_FOUND_BY_ID).process(chat));
                }
            }
        }
        return responses;
    }

    private SendMessage getSendMessageWithSender(String text, Chat sender, User recipient) {
        return SendMessage.builder()
                .chatId(recipient.getChat().getChatId())
                .text("[Від: " + sender.getUser().getFullName() + "]\n" + text)
                .build();
    }
}
