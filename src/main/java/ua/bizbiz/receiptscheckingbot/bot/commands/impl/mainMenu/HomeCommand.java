package ua.bizbiz.receiptscheckingbot.bot.commands.impl.mainMenu;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;

public class HomeCommand extends StartCommand {

    @Override
    public Validable process(Chat chat) {
        return super.process(chat);
    }

    public HomeCommand(Chat chat) {
        super(chat, "Чим ще я можу допомогти вам?");
    }
}
