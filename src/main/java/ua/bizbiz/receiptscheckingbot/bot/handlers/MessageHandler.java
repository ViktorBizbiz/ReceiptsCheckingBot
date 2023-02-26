package ua.bizbiz.receiptscheckingbot.bot.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.bizbiz.receiptscheckingbot.bot.commands.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.AnnouncementCommandType;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.HomeCommandType;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.MainCommandType;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.PromotionCrudCommandType;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.*;
import ua.bizbiz.receiptscheckingbot.persistance.entity.*;
import ua.bizbiz.receiptscheckingbot.persistance.repository.ChatRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.PromotionRepository;
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
    private final PromotionRepository promotionRepository;

    public List<PartialBotApiMethod<Message>> handle(Update update) {
        Chat chat = provideChatRecord(update.getMessage().getChatId());
        String text = update.getMessage().getText();

        List<PartialBotApiMethod<Message>> responses = tryProcessHomeCommand(text, chat);
        if (responses.size() != 0)
            return responses;

        switch (chat.getStatus()) {
            case CREATING_NEW_USER -> responses = processUserDataAdding(text, chat);
            case ENTERING_SECRET_CODE -> responses = processSecretCode(text, chat);
            case AUTHORIZED_AS_ADMIN, AUTHORIZED_AS_USER -> {
                Optional<MainCommandType> command = MainCommandType.parse(text);
                if (command.isPresent()) {
                    responses = processCommand(command.get(), chat);
                }
            }
            case SENDING_ANNOUNCEMENT -> {
                Optional<AnnouncementCommandType> command = AnnouncementCommandType.parse(text);
                if (command.isPresent()) {
                    responses = processCommand(command.get(), chat);
                }
            }
            case SENDING_ANNOUNCEMENT_TO_ALL, SENDING_ANNOUNCEMENT_TO_PERSON ->
                responses = processAnnouncement(text, chat);
            case GETTING_PROMOTIONS -> {
                Optional<PromotionCrudCommandType> command = PromotionCrudCommandType.parse(text);
                if (command.isPresent()) {
                    responses = processCommand(command.get(), chat);
                }
            }
            case CREATING_PROMOTION, UPDATING_PROMOTION, DELETING_PROMOTION ->
                responses = processPromotion(text, chat);
//            case GETTING_REPORT -> ;
        }

        chatRepository.save(chat);
        return responses;
    }

    private List<PartialBotApiMethod<Message>> processPromotion(String text, Chat chat) {
        List<PartialBotApiMethod<Message>> responses = new ArrayList<>();
        String[] splittedText = text.split("\n");
        switch (chat.getStatus()) {
            case CREATING_PROMOTION -> {
                String name = splittedText[0];
                int minQuantity = Integer.parseInt(splittedText[1]);
                int completionBonus = Integer.parseInt(splittedText[2]);
                int resaleBonus = Integer.parseInt(splittedText[3]);
                promotionRepository.save(Promotion.builder()
                        .name(name)
                        .minQuantity(minQuantity)
                        .completionBonus(completionBonus)
                        .resaleBonus(resaleBonus)
                        .build());
                responses.add(new StartCommand(chat.getUser().getRole(),
                        """
                                ✅ Акцію успішно створено.

                                Чим ще я можу допомогти вам?""").process(chat));
            }
            case UPDATING_PROMOTION -> {
                long id = Long.parseLong(splittedText[0]);
                String name = splittedText[1];
                int minQuantity = Integer.parseInt(splittedText[2]);
                int completionBonus = Integer.parseInt(splittedText[3]);
                int resaleBonus = Integer.parseInt(splittedText[4]);
                if (promotionRepository.existsById(id)) {
                    promotionRepository.save(Promotion.builder()
                            .id(id)
                            .name(name)
                            .minQuantity(minQuantity)
                            .completionBonus(completionBonus)
                            .resaleBonus(resaleBonus)
                            .build());
                    responses.add(new StartCommand(chat.getUser().getRole(),
                        """
                                ✅ Акцію успішно змінено.

                                Чим ще я можу допомогти вам?""").process(chat));
                } else {
                    responses.add(new StartCommand(chat.getUser().getRole(),
                        """
                                ⚠️ За даним ID акцій не існує.
                                        
                                Чим ще я можу допомогти вам?""").process(chat));
                }
            }
            case DELETING_PROMOTION -> {
                long id = Long.parseLong(text);
                if (promotionRepository.existsById(id)) {
                    promotionRepository.deleteById(id);
                    responses.add(new StartCommand(chat.getUser().getRole(),
                            """
                                    ✅ Акцію успішно видалено.
    
                                    Чим ще я можу допомогти вам?""").process(chat));
                } else {
                    responses.add(new StartCommand(chat.getUser().getRole(),
                            """
                                    ⚠️ За даним ID акцій не існує.
                                            
                                    Чим ще я можу допомогти вам?""").process(chat));
                }
            }
        }
        return responses;
    }

    private List<PartialBotApiMethod<Message>> processCommand(PromotionCrudCommandType command, Chat chat) {
        List<ProcessableCommand> processableCommands = new ArrayList<>();
        switch (command) {
            case CREATE_PROMOTION -> processableCommands.add(new CreatePromotionCommand());
            case UPDATE_PROMOTION -> processableCommands.add(new UpdatePromotionCommand());
            case DELETE_PROMOTION -> processableCommands.add(new DeletePromotionCommand());
        }
        assert !processableCommands.isEmpty();
        return processableCommands.stream()
                .map(com -> com.process(chat))
                .toList();
    }

    private List<PartialBotApiMethod<Message>> processAnnouncement(String text, Chat chat) {
        List<PartialBotApiMethod<Message>> responses = new ArrayList<>();
        Optional<List<User>> users = userRepository.findAllByRoleAndChatIsNotNull(Role.USER);
        switch (chat.getStatus()) {
            case SENDING_ANNOUNCEMENT_TO_ALL -> {
                if (users.isPresent() && users.get().size() != 0) {
                    for (User user : users.get()) {
                        responses.add(SendMessage.builder()
                                .chatId(user.getChat().getChatId())
                                .text("[Від: " + chat.getUser().getFullName() + "]\n" + text)
                                .build());
                    }
                    responses.add(new StartCommand(chat.getUser().getRole(),
                            "✅ Повідомлення було відправлено усім.").process(chat));
                } else {
                    responses.add(new StartCommand(chat.getUser().getRole(),
                            """
                                    Ще немає жодного користувача, або він ще не авторизувався у боті.
                                    Спочатку додайте хоча б одного та переконайтеся, що він авторизований.

                                    Чим ще я можу допомогти вам?""").process(chat));
                }
            }
            case SENDING_ANNOUNCEMENT_TO_PERSON -> {
                String userId = text.substring(0, text.indexOf("\n"));
                text = text.substring(text.indexOf("\n"));
                Optional<User> user = userRepository.findById(Long.parseLong(userId));
                if (user.isPresent()) {
                    responses.add(SendMessage.builder()
                            .chatId(user.get().getChat().getChatId())
                            .text("[Від: " + chat.getUser().getFullName() + "]\n" + text)
                            .build());
                    responses.add(new StartCommand(chat.getUser().getRole(),
                            "✅ Повідомлення було відправлено.").process(chat));
                } else {
                    responses.add(new StartCommand(chat.getUser().getRole(),
                            """
                                    За таким ID користувачів не існує.

                                    Чим ще я можу допомогти вам?""").process(chat));
                }
            }
        }
        return responses;
    }

    private List<PartialBotApiMethod<Message>> processCommand(AnnouncementCommandType command, Chat chat) {
        List<ProcessableCommand> processableCommands = new ArrayList<>();
        switch (command) {
            case TO_PERSON -> {
                Optional<List<User>> users = userRepository.findAllByRoleAndChatIsNotNull(Role.USER);
                StringBuilder userList = new StringBuilder();
                if (users.isPresent() && users.get().size() != 0) {
                    for (User user : users.get()) {
                        userList.append(user.getUserId()).append(". ").append(user.getFullName()).append("\n");
                    }
                    userList.append("""

                            Введіть ID користувача, якому ви хочете відправити повідомлення та саме повідомлення за наступним шаблоном:
                            ID
                            ваше_повідомлення
                            """);
                    processableCommands.add(new MakeAnnouncementToPersonCommand(userList.toString()));
                } else {
                    processableCommands.add(new StartCommand(chat.getUser().getRole(),
                            """
                                    Ще немає жодного користувача, або він ще не авторизувався у боті.
                                    Спочатку додайте хоча б одного та переконайтеся, що він авторизований.

                                    Чим ще я можу допомогти вам?"""));
                }
            }
            case TO_ALL -> processableCommands.add(new MakeAnnouncementToAllCommand());
        }
        assert !processableCommands.isEmpty();
        return processableCommands.stream()
                .map(com -> com.process(chat))
                .toList();
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
                new StartCommand(chat.getUser().getRole(), "✅ Новий користувач був створений.\n\n" +
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
                Optional<List<User>> reportData = userRepository.findAllByRoleAndChatIsNotNull(Role.USER);
                if (reportData.isPresent() && reportData.get().size() != 0) {
                    processableCommands.add(new CreateReportCommand(reportData.get()));
                    processableCommands.add(new HomeCommand(chat.getUser().getRole()));
                } else {
                    processableCommands.add(new StartCommand(chat.getUser().getRole(),
                            """
                                    Ще немає жодного користувача, або він ще не авторизувався у боті.
                                    Спочатку додайте хоча б одного та переконайтеся, що він авторизований.

                                    Чим ще я можу допомогти вам?"""));
                }
            }
            //TODO change commands
            case ADMIN_SHOW_PROMOTIONS -> {
                List<Promotion> promotions = promotionRepository.findAll();
                if (promotions.size() != 0) {
                    processableCommands.add(new AdminShowPromotionsCommand(promotions));
                } else {
                    processableCommands.add(new AdminShowPromotionsCommand());
                }
            }
            case MAKE_AN_ANNOUNCEMENT -> processableCommands.add(new MakeAnnouncementCommand());
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
