package ua.bizbiz.receiptscheckingbot.bot.processor.text.command.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import ua.bizbiz.receiptscheckingbot.bot.command.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.CommandType;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.HomeCommandTypeMark;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.Markable;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu.DefaultStartCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu.HomeCommand;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.command.CommandProcessor;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class HomeCommandProcessor implements CommandProcessor {
    @Override
    public List<Validable> process(Chat chat, CommandType command) {
        log.info("HomeCommandType detected: " + command);
        final List<ProcessableCommand> processableCommands = new ArrayList<>();
        switch (command) {
            case START -> processableCommands.add(new DefaultStartCommand());
            case HOME -> processableCommands.add(new HomeCommand(chat));
        }
        assert !processableCommands.isEmpty();
        return processableCommands.stream()
                .map(com -> com.process(chat))
                .toList();
    }

    @Override
    public Class<? extends Markable> getMarkClass() {
        return HomeCommandTypeMark.class;
    }
}
