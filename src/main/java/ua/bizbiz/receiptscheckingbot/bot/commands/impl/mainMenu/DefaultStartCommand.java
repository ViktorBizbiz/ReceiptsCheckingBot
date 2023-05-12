package ua.bizbiz.receiptscheckingbot.bot.commands.impl.mainMenu;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ua.bizbiz.receiptscheckingbot.bot.commands.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.ChatStatus;

public class DefaultStartCommand implements ProcessableCommand {

    private final String responseMessageText;
    private final ChatStatus chatStatus;

    @Override
    public Validable process(Chat chat) {
        chat.setStatus(chatStatus);
        return SendMessage.builder()
                .text(responseMessageText)
                .chatId(chat.getChatId())
                .build();
    }

    public DefaultStartCommand() {
        chatStatus = ChatStatus.ENTERING_SECRET_CODE;
        responseMessageText = """
                            Вітаю! Для подальшого користування ботом, пройдіть авторизацію.
                            ✍️ Введіть секретний код доступу.
                            """;
    }
    public DefaultStartCommand(String responseMessageText) {
        chatStatus = ChatStatus.ENTERING_SECRET_CODE;
        this.responseMessageText = responseMessageText;
    }
}