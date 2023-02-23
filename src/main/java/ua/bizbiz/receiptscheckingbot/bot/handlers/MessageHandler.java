package ua.bizbiz.receiptscheckingbot.bot.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.bizbiz.receiptscheckingbot.bot.commands.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.HomeCommandType;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.MainCommandType;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.*;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.ChatStatus;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Role;
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;
import ua.bizbiz.receiptscheckingbot.persistance.repository.ChatRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MessageHandler {

    private static final String TELEGRAM_COMMAND_PREFIX = "/";
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    
    public List<PartialBotApiMethod<Message>> handle(Update update) {
        Chat chat = provideChatRecord(update.getMessage().getChatId());
        String text = update.getMessage().getText();

        List<PartialBotApiMethod<Message>> responses = tryProcessHomeCommand(text, chat);
        if (responses.size() != 0)
            return responses;

        switch (chat.getStatus()) {
            case CREATING_NEW_USER -> responses =
                processUserDataAdding(text, chat);
            case ENTERING_SECRET_CODE -> responses =
                processSecretCode(text, chat);
            case AUTHORIZED_AS_ADMIN, AUTHORIZED_AS_USER -> {
                Optional<MainCommandType> command = MainCommandType.parse(text);
                if (command.isPresent()) {
                    responses = processCommand(command.get(), chat);
                }
            }
            /*case GETTING_REPORT -> ;
            case GETTING_PROMOTIONS -> ;*/
        }

        chatRepository.save(chat);
        return responses;
    }

    private List<PartialBotApiMethod<Message>> processSecretCode(String text, Chat chat) {
        Optional<User> user = userRepository.findBySecretCode(Long.parseLong(text));
        if (user.isPresent()) {
            User existingUser = user.get();
            userRepository.save(User.builder()
                    .userId(existingUser.getUserId())
                    .chat(chat)
                    .phoneNumber(existingUser.getPhoneNumber())
                    .registeredAt(new Timestamp(System.currentTimeMillis()))
                    .soldPackages(0)
                    .score(0)
                    .role(existingUser.getRole())
                    .fullName(existingUser.getFullName())
                    .address(existingUser.getAddress())
                    .pharmacyChain(existingUser.getPharmacyChain())
                    .cityOfPharmacy(existingUser.getCityOfPharmacy())
                    .secretCode(existingUser.getSecretCode())
                    .build());
            Chat chatWithUser = chatRepository.findByChatId(chat.getChatId());
            return Collections.singletonList(new StartCommand(chatWithUser.getUser().getRole(),
                    "✅ Авторизація пройшла успішно.").process(chat));
        }
        return Collections.singletonList(new DefaultStartCommand(
                "⚠️ Невірний код. \nАвторизація не пройдена. Спробуйте ще раз.").process(chat));
    }

    private List<PartialBotApiMethod<Message>> processUserDataAdding(String text, Chat chat) {
        String fullName = text.substring(text.indexOf(" ") + 1,
                text.indexOf("\nАдреса: "));
        String address = text.substring(text.indexOf("\nАдреса: ") + "\nАдреса: ".length(),
                text.indexOf("\nМережа: "));
        String pharmacyChain = text.substring(text.indexOf("\nМережа: ") + "\nМережа: ".length(),
                text.indexOf("\nМісто аптеки: "));
        String cityOfPharmacy = text.substring(text.indexOf("\nМісто аптеки: ") + "\nМісто аптеки: ".length(),
                text.indexOf("\nТелефон: "));
        String phoneNumber = text.substring(text.indexOf("\nТелефон: ") + "\nТелефон: ".length());

        Long secretCode = null;
        int min = 100_000;
        while (userRepository.existsBySecretCode(secretCode) || secretCode == null || secretCode < min)
            secretCode = (long) (Math.random() * 1_000_000);


        userRepository.save(User.builder()
                .fullName(fullName)
                .address(address)
                .pharmacyChain(pharmacyChain)
                .cityOfPharmacy(cityOfPharmacy)
                .phoneNumber(phoneNumber)
                .role(Role.USER)
                .soldPackages(0)
                .score(0)
                .secretCode(secretCode)
                .build());

        return Collections.singletonList(
                new StartCommand(chat.getUser().getRole(), "Новий користувач був створений ✅\n\n" +
                        "\uD83D\uDD10 Перешліть йому цей код доступу: " + secretCode).process(chat));
    }

    private List<PartialBotApiMethod<Message>> tryProcessHomeCommand(String text, Chat chat) {
        List<PartialBotApiMethod<Message>> responses = new ArrayList<>();
        if (isTelegramCommand(text)) {
            text = text.substring(TELEGRAM_COMMAND_PREFIX.length());
        }
        Optional<HomeCommandType> command = HomeCommandType.parse(text);
        if (command.isPresent()) {
            responses = processCommand(command.get(), chat);
            chatRepository.save(chat);
        }
        return responses;
    }

    private List<PartialBotApiMethod<Message>> processCommand(MainCommandType command, Chat chat) {
        List<ProcessableCommand> processableCommands = new ArrayList<>();
        switch (command) {
            case ADD_NEW_USER -> processableCommands.add(new AddUserCommand(ChatStatus.CREATING_NEW_USER));
            case CREATE_REPORT -> {
                var reportData = userRepository.findAll().stream()
                        .filter(user -> user.getRole() == Role.USER)
                        .toList();
                processableCommands.add(new CreateReportCommand(reportData));
                processableCommands.add(new HomeCommand(chat.getUser().getRole()));
            }
            //TODO change commands
            case ADMIN_SHOW_PROMOTIONS -> processableCommands.add(
                    new StartCommand(chat.getUser().getRole(), "На жаль, на даний момент команда недоступна."));
            case MAKE_AN_ANNOUNCEMENT -> processableCommands.add(
                    new StartCommand(chat.getUser().getRole(), "На жаль, на даний момент команда недоступна."));
            case USER_SHOW_PROMOTIONS -> processableCommands.add(
                    new StartCommand(chat.getUser().getRole(), "На жаль, на даний момент команда недоступна."));
            case SEND_RECEIPT -> processableCommands.add(
                    new StartCommand(chat.getUser().getRole(), "На жаль, на даний момент команда недоступна."));
            case BALANCE -> processableCommands.add(
                    new StartCommand(chat.getUser().getRole(), "На жаль, на даний момент команда недоступна."));
        }
        assert !processableCommands.isEmpty();
        return processableCommands.stream()
                .map(com -> com.process(chat))
                .toList();
    }

    private List<PartialBotApiMethod<Message>> processCommand(HomeCommandType command, Chat chat) {
        ProcessableCommand processableCommand = null;
        switch (command) {
            case START -> processableCommand = new DefaultStartCommand();
            case HOME -> processableCommand = new HomeCommand(chat.getUser().getRole());
        }
        assert processableCommand != null;
        return Collections.singletonList(processableCommand.process(chat));
    }

    private boolean isTelegramCommand(String text) {
        return text.startsWith(TELEGRAM_COMMAND_PREFIX);
    }

    private Chat provideChatRecord(Long chatId) {
        Chat chat = chatRepository.findByChatId(chatId);
        if (chat == null) {
            chat = Chat.builder()
                    .chatId(chatId)
                    .build();
            chatRepository.save(chat);
        }
        return chat;
    }
}
