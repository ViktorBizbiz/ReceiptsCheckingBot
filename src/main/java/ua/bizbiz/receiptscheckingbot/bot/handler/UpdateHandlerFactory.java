package ua.bizbiz.receiptscheckingbot.bot.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UpdateHandlerFactory {

    private final Map<Class<? extends UpdateHandler>, UpdateHandler> updateHandlerMap;

    public UpdateHandlerFactory(List<UpdateHandler> updateHandlers) {
        this.updateHandlerMap = updateHandlers.stream()
                .collect(Collectors.toMap(UpdateHandler::getClass, Function.identity()));
    }

    public UpdateHandler getUpdateHandler(Update update) {
        if (update.hasCallbackQuery()) {
            return updateHandlerMap.get(CallbackHandler.class);
        } else if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                return updateHandlerMap.get(MessageHandler.class);
            } else if (update.getMessage().hasPhoto()) {
                return updateHandlerMap.get(PhotoHandler.class);
            }
        }
        return null;
    }
}