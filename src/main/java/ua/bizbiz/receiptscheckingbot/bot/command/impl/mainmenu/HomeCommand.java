package ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.WHAT_ELSE_HELP_YOU_NEED;

public class HomeCommand extends StartCommand {

    @Override
    public Validable process(Chat chat) {
        return super.process(chat);
    }

    public HomeCommand(Chat chat) {
        super(chat, WHAT_ELSE_HELP_YOU_NEED);
    }
}
