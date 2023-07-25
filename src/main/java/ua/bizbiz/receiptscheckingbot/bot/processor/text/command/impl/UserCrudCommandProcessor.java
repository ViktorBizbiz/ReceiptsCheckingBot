package ua.bizbiz.receiptscheckingbot.bot.processor.text.command.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import ua.bizbiz.receiptscheckingbot.bot.command.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.CommandType;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.Markable;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.crud.UserCrudCommandTypeMark;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.user.CreateUserCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.user.DeleteUserCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.user.ReadUserCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.user.UpdateUserCommand;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.command.CommandProcessor;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class UserCrudCommandProcessor implements CommandProcessor {
    @Override
    public List<Validable> process(Chat chat, CommandType command) {
        log.info("UserCrudCommandType detected: " + command);
        final List<ProcessableCommand> processableCommands = new ArrayList<>();
        switch (command) {
            case CREATE_USER -> processableCommands.add(new CreateUserCommand());
            case READ_USER -> processableCommands.add(new ReadUserCommand());
            case UPDATE_USER -> processableCommands.add(new UpdateUserCommand());
            case DELETE_USER -> processableCommands.add(new DeleteUserCommand());
        }
        assert !processableCommands.isEmpty();
        return processableCommands.stream()
                .map(com -> com.process(chat))
                .toList();
    }

    @Override
    public Class<? extends Markable> getMarkClass() {
        return UserCrudCommandTypeMark.class;
    }
}
