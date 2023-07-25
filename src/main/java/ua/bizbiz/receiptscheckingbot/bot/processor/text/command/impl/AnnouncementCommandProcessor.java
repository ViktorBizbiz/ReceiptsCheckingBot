package ua.bizbiz.receiptscheckingbot.bot.processor.text.command.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import ua.bizbiz.receiptscheckingbot.bot.command.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.AnnouncementCommandMark;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.CommandType;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.Markable;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.announcement.MakeAnnouncementToAllCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.announcement.MakeAnnouncementToPersonCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu.StartCommand;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.command.CommandProcessor;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Role;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.NO_AUTHORIZED_USER_FOUND;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnnouncementCommandProcessor implements CommandProcessor {

    private final UserRepository userRepository;

    @Override
    public List<Validable> process(Chat chat, CommandType command) {
        log.info("AnnouncementCommandType detected: " + command);
        final List<ProcessableCommand> processableCommands = new ArrayList<>();
        switch (command) {
            case TO_PERSON -> {
                final var users = userRepository.findAllByRoleAndChatIsNotNull(Role.USER);
                if (users.isPresent() && !users.get().isEmpty()) {
                    processableCommands.add(new MakeAnnouncementToPersonCommand(users.get()));
                } else {
                    processableCommands.add(new StartCommand(chat, NO_AUTHORIZED_USER_FOUND));
                }
            }
            case TO_ALL -> processableCommands.add(new MakeAnnouncementToAllCommand());
        }
        assert !processableCommands.isEmpty();
        return processableCommands.stream()
                .map(com -> com.process(chat))
                .toList();
    }

    @Override
    public Class<? extends Markable> getMarkClass() {
        return AnnouncementCommandMark.class;
    }
}
