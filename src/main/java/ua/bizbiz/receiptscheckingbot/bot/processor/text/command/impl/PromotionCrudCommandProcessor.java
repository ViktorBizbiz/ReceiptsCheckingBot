package ua.bizbiz.receiptscheckingbot.bot.processor.text.command.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import ua.bizbiz.receiptscheckingbot.bot.command.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.CommandType;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.Markable;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.crud.PromotionCrudCommandTypeMark;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.promotion.CreatePromotionCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.promotion.DeletePromotionCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.promotion.UpdatePromotionCommand;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.command.CommandProcessor;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PromotionCrudCommandProcessor implements CommandProcessor {
    @Override
    public List<Validable> process(Chat chat, CommandType command) {
        log.info("PromotionCrudCommandType detected: " + command);
        final List<ProcessableCommand> processableCommands = new ArrayList<>();
        switch (command) {
            case CREATE_PROMOTION -> processableCommands.add(new CreatePromotionCommand());
            case UPDATE_PROMOTION -> processableCommands.add(new UpdatePromotionCommand());
            case DELETE_PROMOTION -> processableCommands.add(new DeletePromotionCommand());
        }
        assert !processableCommands.isEmpty();
        return processableCommands.stream()
                .map(com -> com.process(chat))
                .toList();
    }

    @Override
    public Class<? extends Markable> getMarkClass() {
        return PromotionCrudCommandTypeMark.class;
    }
}
