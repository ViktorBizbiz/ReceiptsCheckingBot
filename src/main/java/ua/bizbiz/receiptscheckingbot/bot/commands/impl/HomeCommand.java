package ua.bizbiz.receiptscheckingbot.bot.commands.impl;

import org.telegram.telegrambots.meta.api.interfaces.Validable;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Role;

public class HomeCommand extends StartCommand {

    @Override
    public Validable process(Chat chat) {
        return super.process(chat);
    }

    public HomeCommand(Role role) {
        super(role, "Чим ще я можу допомогти вам?");
    }
}
