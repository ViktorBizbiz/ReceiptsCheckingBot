package ua.bizbiz.receiptscheckingbot.util;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;

import java.util.ArrayList;
import java.util.List;

public class DeleteUtils {
    public static List<DeleteMessage> deleteMessages(int messageId, int quantityAboveToDelete, Chat chat) {
        List<DeleteMessage> deleteMessages = new ArrayList<>();
        for (int i = messageId; i >= messageId - quantityAboveToDelete; i--) {
            deleteMessages.add(new DeleteMessage(chat.getChatId().toString(), i));
        }
        return deleteMessages;
    }

    public static DeleteMessage deleteMessage(int messageId, Chat chat) {
        return new DeleteMessage(chat.getChatId().toString(), messageId);
    }
}
