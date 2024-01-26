package ua.bizbiz.receiptscheckingbot.bot.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ua.bizbiz.receiptscheckingbot.bot.TelegramBot;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Role;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class UserReminderScheduler {

    private final TelegramBot bot;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 9 * * *")
    private void sendMorningReminder() {
        sendTextForAllUsers("""
                Доброго ранку!👋🏻 Бажаю Вам продуктивного дня.🔋
                Нагадую, що всі актуальні акції на сьогодні можна обрати натиснувши "Актуальні акції 💊".
                """);
    }

    @Scheduled(cron = "0 0 18 * * *")
    private void sendEveningReminder() {
        sendTextForAllUsers("""
                Доброго вечора!👋🏻 Ось вже і кінець робочого дня. 🕕
                Пропоную перевірити Ваш баланс, натиснувши "Баланс 💰".
                А також, не забудьте внести усі фіскальні чеки за сьогодні на перевірку (кнопка "Відправити чек 🧾"), щоб не втратити прогрес. 📈
                """);
    }

    private void sendTextForAllUsers(String text) {
        userRepository.findAllByRoleAndChatIsNotNull(Role.USER)
                .ifPresent(users -> users
                        .forEach(user -> bot.execute(SendMessage.builder()
                                .text(text)
                                .chatId(user.getChat().getChatId())
                                .build())));
    }
}
