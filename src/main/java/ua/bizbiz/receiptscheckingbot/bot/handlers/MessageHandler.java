package ua.bizbiz.receiptscheckingbot.bot.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
import ua.bizbiz.receiptscheckingbot.persistance.repository.SubscriptionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;
import ua.bizbiz.receiptscheckingbot.util.DeleteUtils;

import java.sql.Timestamp;
import java.util.*;

@Component
@RequiredArgsConstructor
public class MessageHandler {

    private static final String TELEGRAM_COMMAND_PREFIX = "/";
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final PromotionRepository promotionRepository;
    private final SubscriptionRepository subscriptionRepository;

    public List<Validable> handle(Update update) {
        Chat chat = provideChatRecord(update.getMessage().getChatId());
        String text = update.getMessage().getText();
        int messageId = update.getMessage().getMessageId();

        List<Validable> responses = tryProcessHomeCommand(text, chat);

        if (responses.size() != 0)
            return responses;

        switch (chat.getStatus()) {
            case CREATING_NEW_USER -> responses.addAll(processUserDataAdding(text, chat));
            case ENTERING_SECRET_CODE -> responses.addAll(processSecretCode(text, chat, messageId));
            case AUTHORIZED_AS_ADMIN, AUTHORIZED_AS_USER -> {
                Optional<MainCommandType> command = MainCommandType.parse(text);
                command.ifPresent(mainCommandType ->
                        responses.addAll(processCommand(mainCommandType, chat)));
            }
            case SENDING_ANNOUNCEMENT -> {
                Optional<AnnouncementCommandType> command = AnnouncementCommandType.parse(text);
                command.ifPresent(announcementCommandType ->
                        responses.addAll(processCommand(announcementCommandType, chat)));
            }
            case SENDING_ANNOUNCEMENT_TO_ALL, SENDING_ANNOUNCEMENT_TO_PERSON ->
                responses.addAll(processAnnouncement(text, chat));
            case ADMIN_GETTING_PROMOTIONS -> {
                Optional<PromotionCrudCommandType> command = PromotionCrudCommandType.parse(text);
                command.ifPresent(promotionCrudCommandType ->
                        responses.addAll(processCommand(promotionCrudCommandType, chat)));
            }
            case CREATING_PROMOTION, UPDATING_PROMOTION, DELETING_PROMOTION ->
                responses.addAll(processPromotion(text, chat));
//            case GETTING_REPORT -> ;
        }

        chatRepository.save(chat);
        return responses;
    }

    private List<Validable> processPromotion(String text, Chat chat) {
        List<Validable> responses = new ArrayList<>();
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

    private List<Validable> processCommand(PromotionCrudCommandType command, Chat chat) {
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

    private List<Validable> processAnnouncement(String text, Chat chat) {
        List<Validable> responses = new ArrayList<>();
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

    private List<Validable> processCommand(AnnouncementCommandType command, Chat chat) {
        List<ProcessableCommand> processableCommands = new ArrayList<>();
        switch (command) {
            case TO_PERSON -> {
                Optional<List<User>> users = userRepository.findAllByRoleAndChatIsNotNull(Role.USER);
                StringBuilder userList = new StringBuilder();
                if (users.isPresent() && users.get().size() != 0) {
                    for (User user : users.get()) {
                        userList.append(user.getId()).append(". ").append(user.getFullName()).append("\n");
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

    private List<Validable> processSecretCode(String text, Chat chat, int messageId) {
        List<Validable> responses = new ArrayList<>();
        long secretCode = Long.parseLong(text);
        Optional<User> user = userRepository.findBySecretCode(secretCode);
        if (user.isPresent()) {
            User existingUser = user.get();
            if (userRepository.existsBySecretCodeAndChatIsNull(secretCode)) {
                userRepository.save(User.builder()
                        .id(existingUser.getId())
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
            } else {
                if (!existingUser.getChat().getChatId().equals(chat.getChatId())) {
                    responses.addAll(DeleteUtils.deleteMessages(messageId, 1, chat));
                    responses.add(new DefaultStartCommand(
                            "⚠️ Невірний код. \nАвторизація не пройдена. Спробуйте ще раз.").process(chat));
                    return responses;
                }
            }

            Chat chatWithUser = chatRepository.findByChatId(chat.getChatId());

            responses.addAll(DeleteUtils.deleteMessages(messageId, 1, chat));
            responses.add(new StartCommand(chatWithUser.getUser().getRole(),
                    "✅ Авторизація пройшла успішно.").process(chat));
        } else {
            responses.addAll(DeleteUtils.deleteMessages(messageId, 1, chat));
            responses.add(new DefaultStartCommand(
                    "⚠️ Невірний код. \nАвторизація не пройдена. Спробуйте ще раз.").process(chat));
        }
        return responses;
    }

    private List<Validable> processUserDataAdding(String text, Chat chat) {
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

    private List<Validable> tryProcessHomeCommand(String text, Chat chat) {
        List<Validable> responses = new ArrayList<>();
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

    private List<Validable> processCommand(MainCommandType command, Chat chat) {
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
            case CHECK_RECEIPTS -> processableCommands.add(new CheckReceiptsCommand());
            case USER_SHOW_PROMOTIONS -> {
                List<Promotion> promotions = promotionRepository.findAll();
                if (promotions.size() != 0) {
                    processableCommands.add(new UserShowPromotionsCommand(promotions, chat));
                } else {
                    processableCommands.add(new StartCommand(chat.getUser().getRole(),
                            """
                                    Ще немає жодної акції.

                                    Чим ще я можу допомогти вам?"""));
                }
            }
            case SEND_RECEIPT -> {
                List<Subscription> subscriptions = subscriptionRepository.findAllByUserId(chat.getUser().getId());
                if (subscriptions.size() != 0) {
                    processableCommands.add(new SendReceiptCommand(subscriptions));
                } else {
                    processableCommands.add(new StartCommand(chat.getUser().getRole(),
                            "⚠️ У вас ще немає жодної підписки.\nСпочатку підпишіться хоча б на одну акцію."));
                }
            }
            case BALANCE -> processableCommands.add(
                    new StartCommand(chat.getUser().getRole(), "На жаль, на даний момент команда недоступна."));
        }
        assert !processableCommands.isEmpty();
        return processableCommands.stream()
                .map(com -> com.process(chat))
                .toList();
    }

    private List<Validable> processCommand(HomeCommandType command, Chat chat) {
        List<Validable> responses = new ArrayList<>();
        switch (command) {
            case START -> responses.add(new DefaultStartCommand().process(chat));
            case HOME -> responses.add(new HomeCommand(chat.getUser().getRole()).process(chat));
        }
        return responses;
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
