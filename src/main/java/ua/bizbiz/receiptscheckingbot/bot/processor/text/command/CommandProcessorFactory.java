package ua.bizbiz.receiptscheckingbot.bot.processor.text.command;

import org.springframework.stereotype.Component;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.CommandType;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.Markable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CommandProcessorFactory {

    private final Map<Class<? extends Markable>, CommandProcessor> commandProcessorMap;

    public CommandProcessorFactory(List<CommandProcessor> commandProcessors) {
        this.commandProcessorMap = commandProcessors.stream()
                .collect(Collectors.toMap(CommandProcessor::getMarkClass, Function.identity()));
    }

    public CommandProcessor getCommandProcessor(CommandType command) {
        return commandProcessorMap.get(command.getClassMarkable());
    }
}
