package ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ua.bizbiz.receiptscheckingbot.bot.command.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.ChatStatus;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.ENTER_AUTHORIZATION_CODE;

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
        responseMessageText = ENTER_AUTHORIZATION_CODE;
    }
    public DefaultStartCommand(String responseMessageText) {
        chatStatus = ChatStatus.ENTERING_SECRET_CODE;
        this.responseMessageText = responseMessageText;
    }
}
