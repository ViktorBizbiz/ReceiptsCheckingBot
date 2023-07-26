package ua.bizbiz.receiptscheckingbot.bot.processor.callback;

import org.springframework.stereotype.Component;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.CommandType;
import ua.bizbiz.receiptscheckingbot.bot.processor.callback.impl.CheckReceiptProcessor;
import ua.bizbiz.receiptscheckingbot.bot.processor.callback.impl.ChosenSubscriptionProcessor;
import ua.bizbiz.receiptscheckingbot.bot.processor.callback.impl.HomeProcessor;
import ua.bizbiz.receiptscheckingbot.bot.processor.callback.impl.UserSubscriptionsProcessor;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CallbackProcessorFactory {

    private final Map<Class<? extends CallbackProcessor>, CallbackProcessor> callbackProcessorMap;

    public CallbackProcessorFactory(List<CallbackProcessor> callbackProcessors) {
        this.callbackProcessorMap = callbackProcessors.stream()
                .collect(Collectors.toMap(CallbackProcessor::getClass, Function.identity()));
    }

    public CallbackProcessor getCallbackProcessor(Chat chat, String[] callbackData) {

        if (callbackData[0].equals(CommandType.HOME.getName())) {
            return callbackProcessorMap.get(HomeProcessor.class);
        }

        switch (chat.getStatus()) {
            case USER_GETTING_PROMOTIONS -> {
                return callbackProcessorMap.get(UserSubscriptionsProcessor.class);
            }
            case SENDING_RECEIPT -> {
                return callbackProcessorMap.get(ChosenSubscriptionProcessor.class);
            }
            case CHECKING_RECEIPTS -> {
                return callbackProcessorMap.get(CheckReceiptProcessor.class);
            }
            default -> {
                return null;
            }
        }
    }
}
