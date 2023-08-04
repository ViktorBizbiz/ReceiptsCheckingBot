package ua.bizbiz.receiptscheckingbot.bot.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ua.bizbiz.receiptscheckingbot.bot.TelegramBot;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Role;
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class UserMotivationScheduler {

    private final TelegramBot bot;
    private final UserRepository userRepository;

    @SneakyThrows
    @Scheduled(cron = "0 0 9,16 * * *")
    private void sendMotivationText() {
        var users = userRepository.findAllByRoleAndChatIsNotNull(Role.USER);
        if (users.isPresent()) {
            for (User user : users.get()) {
                String motivation = "\uD83D\uDE00 Motivation \uD83D\uDE00";
                bot.execute(SendMessage.builder()
                        .text(motivation)
                        .chatId(user.getChat().getChatId())
                        .build());
            }
        }
    }
}
