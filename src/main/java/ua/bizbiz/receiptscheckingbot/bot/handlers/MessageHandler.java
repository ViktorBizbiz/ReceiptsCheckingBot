package ua.bizbiz.receiptscheckingbot.bot.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.bizbiz.receiptscheckingbot.bot.commands.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.AnnouncementCommandType;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.HomeCommandType;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.MainCommandType;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.crud.PromotionCrudCommandType;
import ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.crud.UserCrudCommandType;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.BalanceCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.CheckReceiptsCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.CreateReportCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.SendReceiptCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.announcement.MakeAnnouncementCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.announcement.MakeAnnouncementToAllCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.announcement.MakeAnnouncementToPersonCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.mainMenu.DefaultStartCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.mainMenu.HomeCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.mainMenu.StartCommand;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.promotion.*;
import ua.bizbiz.receiptscheckingbot.bot.commands.impl.user.*;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Promotion;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Role;
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;
import ua.bizbiz.receiptscheckingbot.persistance.repository.ChatRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.PromotionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.SubscriptionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;
import ua.bizbiz.receiptscheckingbot.util.DeleteUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.*;
import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.Command.TELEGRAM_COMMAND_PREFIX;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageHandler {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final PromotionRepository promotionRepository;
    private final SubscriptionRepository subscriptionRepository;

    public List<Validable> handle(Update update) {
        final var chat = provideChatRecord(update.getMessage().getChatId());
        final var text = update.getMessage().getText();
        final var messageId = update.getMessage().getMessageId();

        List<Validable> responses = tryProcessHomeCommand(text, chat);

        if (responses.size() != 0)
            return responses;

        log.info("Update handling with status: " + chat.getStatus());
        switch (chat.getStatus()) {
            case ADMIN_GETTING_USERS -> UserCrudCommandType.parse(text).ifPresent(command ->
                        responses.addAll(processCommand(command, chat)));
            case CREATING_USER, READING_USER, UPDATING_USER, DELETING_USER ->
                    responses.addAll(processUser(text, chat));
            case ENTERING_SECRET_CODE -> responses.addAll(processSecretCode(text, chat, messageId));
            case AUTHORIZED_AS_ADMIN, AUTHORIZED_AS_USER -> MainCommandType.parse(text).ifPresent(command ->
                        responses.addAll(processCommand(command, chat)));
            case SENDING_ANNOUNCEMENT -> AnnouncementCommandType.parse(text).ifPresent(command ->
                        responses.addAll(processCommand(command, chat)));
            case SENDING_ANNOUNCEMENT_TO_ALL, SENDING_ANNOUNCEMENT_TO_PERSON ->
                responses.addAll(processAnnouncement(text, chat));
            case ADMIN_GETTING_PROMOTIONS -> PromotionCrudCommandType.parse(text).ifPresent(command ->
                        responses.addAll(processCommand(command, chat)));
            case CREATING_PROMOTION, UPDATING_PROMOTION, DELETING_PROMOTION ->
                responses.addAll(processPromotion(text, chat));
        }

        chatRepository.save(chat);
        return responses;
    }


    private List<Validable> processPromotion(String text, Chat chat) {
        List<Validable> responses = new ArrayList<>();
        String[] splittedText = text.split("\n");
        switch (chat.getStatus()) {
            case CREATING_PROMOTION -> {
                final var name = splittedText[0];
                final var minQuantity = Integer.parseInt(splittedText[1]);
                final var completionBonus = Integer.parseInt(splittedText[2]);
                final var resaleBonus = Integer.parseInt(splittedText[3]);
                promotionRepository.save(Promotion.builder()
                        .name(name)
                        .minQuantity(minQuantity)
                        .completionBonus(completionBonus)
                        .resaleBonus(resaleBonus)
                        .build());
                responses.add(new StartCommand(chat, PROMOTION_CREATED_SUCCESSFULLY).process(chat));
            }
            case UPDATING_PROMOTION -> {
                final var id = Long.parseLong(splittedText[0]);
                final var name = splittedText[1];
                final var minQuantity = Integer.parseInt(splittedText[2]);
                final var completionBonus = Integer.parseInt(splittedText[3]);
                final var resaleBonus = Integer.parseInt(splittedText[4]);
                if (!promotionRepository.existsById(id)) {
                    responses.add(new StartCommand(chat, NO_PROMOTION_FOUND_BY_ID).process(chat));
                }
                promotionRepository.save(Promotion.builder()
                        .id(id)
                        .name(name)
                        .minQuantity(minQuantity)
                        .completionBonus(completionBonus)
                        .resaleBonus(resaleBonus)
                        .build());
                responses.add(new StartCommand(chat, PROMOTION_UPDATED_SUCCESSFULLY).process(chat));
            }
            case DELETING_PROMOTION -> {
                final var id = Long.parseLong(text);
                if (!promotionRepository.existsById(id)) {
                    responses.add(new StartCommand(chat, NO_PROMOTION_FOUND_BY_ID).process(chat));
                }
                promotionRepository.deleteById(id);
                responses.add(new StartCommand(chat, PROMOTION_DELETED_SUCCESSFULLY).process(chat));
            }
        }
        return responses;
    }

    private List<Validable> processAnnouncement(String text, Chat chat) {
        final List<Validable> responses = new ArrayList<>();
        final var users = userRepository.findAllByRoleAndChatIsNotNull(Role.USER);
        switch (chat.getStatus()) {
            case SENDING_ANNOUNCEMENT_TO_ALL -> {
                if (users.isPresent() && users.get().size() != 0) {
                    for (User user : users.get())
                        responses.add(getSendMessageWithSender(text, chat, user));
                    responses.add(new StartCommand(chat, MESSAGE_SENT_SUCCESSFULLY).process(chat));
                } else {
                    responses.add(new StartCommand(chat, NO_AUTHORIZED_USER_FOUND).process(chat));
                }
            }
            case SENDING_ANNOUNCEMENT_TO_PERSON -> {
                final var userId = Long.parseLong(text.substring(0, text.indexOf("\n")));
                text = text.substring(text.indexOf("\n"));
                final var user = userRepository.findById(userId);
                if (user.isPresent()) {
                    responses.add(getSendMessageWithSender(text, chat, user.get()));
                    responses.add(new StartCommand(chat, MESSAGE_SENT_SUCCESSFULLY).process(chat));
                } else {
                    responses.add(new StartCommand(chat, NO_USER_FOUND_BY_ID).process(chat));
                }
            }
        }
        return responses;
    }

    private List<Validable> processSecretCode(String text, Chat chat, int messageId) {
        final List<Validable> responses = new ArrayList<>();
        final var secretCode = Long.parseLong(text);
        final var optionalUser = userRepository.findBySecretCode(secretCode);
        // if user not exists
        if (optionalUser.isEmpty()) {
            provideAuthenticationFailedMessage(chat, messageId, responses);
            return responses;
        }
        final var user = optionalUser.get();
        final var userChat = user.getChat();
        // if user's chat exists, and it's not the same as current chat
        if (userChat != null &&
                !userChat.getChatId().equals(chat.getChatId())) {
            provideAuthenticationFailedMessage(chat, messageId, responses);
            return responses;
        }
        user.setChat(chat);
        chat.setUser(user);
        user.setRegisteredAt(LocalDateTime.now());
        userRepository.save(user);

        responses.addAll(DeleteUtils.deleteMessages(messageId, 1, chat));
        responses.add(new StartCommand(chat, SUCCESSFUL_AUTHORIZATION).process(chat));

        log.info("Authentication success");
        return responses;
    }

    private List<Validable> processUser(String text, Chat chat) {
        //TODO: Скрыть реализацию в отдельных классах(придумать как)
        final List<Validable> responses = new ArrayList<>();
        final var splittedText = text.split("\n");
        switch (chat.getStatus()) {
            case CREATING_USER -> {
                final var fullName = splittedText[0];
                final var address = splittedText[1];
                final var pharmacyChain = splittedText[2];
                final var cityOfPharmacy = splittedText[3];
                final var phoneNumber = splittedText[4];
                final var secretCode = generateSecretCode();

                userRepository.save(User.builder()
                        .fullName(fullName)
                        .address(address)
                        .pharmacyChain(pharmacyChain)
                        .cityOfPharmacy(cityOfPharmacy)
                        .phoneNumber(phoneNumber)
                        .role(Role.USER)
                        .secretCode(secretCode)
                        .build());

                responses.add(new StartCommand(chat,
                        String.format(USER_CREATED_SUCCESSFULLY, secretCode)).process(chat));
            }
            case READING_USER -> {
                final var id = Long.parseLong(text);
                final var user = getUserIfExists(id, chat, responses);
                if (user == null)
                    return responses;
                responses.add(new StartCommand(chat,
                        String.format(USER_PROFILE_DATA,
                                user.getId(),
                                user.getFullName(),
                                user.getAddress(),
                                user.getPharmacyChain(),
                                user.getCityOfPharmacy(),
                                user.getPhoneNumber()
                        )).process(chat));
            }
            case UPDATING_USER -> {
                final var id = Long.parseLong(splittedText[0]);
                final var fullName = splittedText[1];
                final var address = splittedText[2];
                final var pharmacyChain = splittedText[3];
                final var cityOfPharmacy = splittedText[4];
                final var phoneNumber = splittedText[5];
                final var user = getUserIfExists(id, chat, responses);
                if (user == null)
                    return responses;
                user.setFullName(fullName);
                user.setAddress(address);
                user.setPharmacyChain(pharmacyChain);
                user.setCityOfPharmacy(cityOfPharmacy);
                user.setPhoneNumber(phoneNumber);
                userRepository.save(user);
                responses.add(new StartCommand(chat, USER_UPDATED_SUCCESSFULLY).process(chat));
            }
            case DELETING_USER -> {
                final var id = Long.parseLong(text);
                final var user = getUserIfExists(id, chat, responses);
                if (user == null)
                    return responses;
                final var userChat = user.getChat();
                // if user has chat, and it's the same as current chat
                if (userChat != null && userChat.getChatId().equals(chat.getChatId())) {
                    responses.add(new StartCommand(chat, CANNOT_DELETE_YOURSELF).process(chat));
                    return responses;
                }
                userRepository.deleteById(id);
                responses.add(new StartCommand(chat, USER_DELETED_SUCCESSFULLY).process(chat));
            }
        }
        return responses;
    }

    private List<Validable> tryProcessHomeCommand(String text, Chat chat) {
        final List<Validable> responses = new ArrayList<>();
        if (isTelegramCommand(text))
            text = text.substring(TELEGRAM_COMMAND_PREFIX.length());
        HomeCommandType.parse(text).ifPresent(command -> responses.addAll(processCommand(command, chat)));

        chatRepository.save(chat);
        return responses;
    }

    private List<Validable> processCommand(AnnouncementCommandType command, Chat chat) {
        log.info("AnnouncementCommandType detected: " + command);
        final List<ProcessableCommand> processableCommands = new ArrayList<>();
        switch (command) {
            case TO_PERSON -> {
                final var users = userRepository.findAllByRoleAndChatIsNotNull(Role.USER);
                if (users.isPresent() && users.get().size() != 0) {
                    processableCommands.add(new MakeAnnouncementToPersonCommand(users.get()));
                } else {
                    processableCommands.add(new StartCommand(chat, NO_AUTHORIZED_USER_FOUND));
                }
            }
            case TO_ALL -> processableCommands.add(new MakeAnnouncementToAllCommand());
        }
        assert !processableCommands.isEmpty();
        return processableCommands.stream()
                .map(com -> com.process(chat))
                .toList();
    }

    private List<Validable> processCommand(PromotionCrudCommandType command, Chat chat) {
        log.info("PromotionCrudCommandType detected: " + command);
        final List<ProcessableCommand> processableCommands = new ArrayList<>();
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

    private List<Validable> processCommand(UserCrudCommandType command, Chat chat) {
        log.info("UserCrudCommandType detected: " + command);
        final List<ProcessableCommand> processableCommands = new ArrayList<>();
        switch (command) {
            case CREATE_USER -> processableCommands.add(new CreateUserCommand());
            case READ_USER -> processableCommands.add(new ReadUserCommand());
            case UPDATE_USER -> processableCommands.add(new UpdateUserCommand());
            case DELETE_USER -> processableCommands.add(new DeleteUserCommand());
        }
        assert !processableCommands.isEmpty();
        return processableCommands.stream()
                .map(com -> com.process(chat))
                .toList();
    }

    private List<Validable> processCommand(MainCommandType command, Chat chat) {
        log.info("MainCommandType detected: " + command);
        final List<ProcessableCommand> processableCommands = new ArrayList<>();
        switch (command) {
            case ADMIN_SHOW_USERS -> {
                final var users = userRepository.findAll();
                if (users.size() != 0) {
                    processableCommands.add(new AdminShowUsersCommand(users));
                } else {
                    throw new RuntimeException("Something went wrong [users.size() == 0]");
                }
            }
            case CREATE_REPORT -> {
                final var reportData = subscriptionRepository.findAll();
                final var promotions = promotionRepository.findAll();
                if (reportData.size() != 0 && promotions.size() != 0) {
                    processableCommands.add(new CreateReportCommand(reportData, promotions));
                    processableCommands.add(new HomeCommand(chat));
                } else {
                    processableCommands.add(new StartCommand(chat, NO_SUBSCRIPTION_FOUND_2));
                }
            }
            case ADMIN_SHOW_PROMOTIONS -> {
                final var promotions = promotionRepository.findAll();
                if (promotions.size() != 0) {
                    processableCommands.add(new AdminShowPromotionsCommand(promotions));
                } else {
                    processableCommands.add(new AdminShowPromotionsCommand());
                }
            }
            case MAKE_AN_ANNOUNCEMENT -> processableCommands.add(new MakeAnnouncementCommand());
            case CHECK_RECEIPTS -> processableCommands.add(new CheckReceiptsCommand());
            case USER_SHOW_PROMOTIONS -> {
                final var promotions = promotionRepository.findAll();
                final var userSubscriptions = subscriptionRepository.findAllByUserId(chat.getUser().getId());
                if (promotions.size() != 0) {
                    processableCommands.add(new UserShowPromotionsCommand(promotions, userSubscriptions));
                } else {
                    processableCommands.add(new StartCommand(chat, NO_PROMOTION_FOUND));
                }
            }
            case SEND_RECEIPT -> {
                final var subscriptions = subscriptionRepository.findAllByUserId(chat.getUser().getId());
                if (subscriptions.size() != 0) {
                    processableCommands.add(new SendReceiptCommand(subscriptions));
                } else {
                    processableCommands.add(new StartCommand(chat, NO_SUBSCRIPTION_FOUND_1));
                }
            }
            case BALANCE -> {
                final var subscriptions = subscriptionRepository.findAllByUserId(chat.getUser().getId());
                if (subscriptions.size() != 0) {
                    processableCommands.add(new BalanceCommand(subscriptions));
                    processableCommands.add(new HomeCommand(chat));
                } else {
                    processableCommands.add(new StartCommand(chat, NO_SUBSCRIPTION_FOUND_1));
                }
            }
        }
        assert !processableCommands.isEmpty();
        return processableCommands.stream()
                .map(com -> com.process(chat))
                .toList();
    }

    private List<Validable> processCommand(HomeCommandType command, Chat chat) {
        log.info("HomeCommandType detected: " + command);
        final List<Validable> responses = new ArrayList<>();
        switch (command) {
            case START -> responses.add(new DefaultStartCommand().process(chat));
            case HOME -> responses.add(new HomeCommand(chat).process(chat));
        }
        return responses;
    }

    private void provideAuthenticationFailedMessage(Chat chat, int messageId, List<Validable> responses) {
        responses.addAll(DeleteUtils.deleteMessages(messageId, 1, chat));
        responses.add(new DefaultStartCommand(WRONG_AUTHORIZATION_CODE).process(chat));
        log.info("Authentication not passed");
    }

    private SendMessage getSendMessageWithSender(String text, Chat sender, User recipient) {
        return SendMessage.builder()
                .chatId(recipient.getChat().getChatId())
                .text("[Від: " + sender.getUser().getFullName() + "]\n" + text)
                .build();
    }

    private User getUserIfExists(long id, Chat chat, List<Validable> responses) {
        final var optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            responses.add(new StartCommand(chat, NO_USER_FOUND_BY_ID).process(chat));
            return null;
        }
        return optionalUser.get();
    }

    private long generateSecretCode() {
        Long secretCode = null;
        final var min = 100_000;
        while (userRepository.existsBySecretCode(secretCode) || secretCode == null || secretCode < min)
            secretCode = (long) (Math.random() * 1_000_000);
        return secretCode;
    }

    private boolean isTelegramCommand(String text) {
        return text.startsWith(TELEGRAM_COMMAND_PREFIX);
    }

    private Chat provideChatRecord(Long chatId) {
        var chat = chatRepository.findByChatId(chatId);
        if (chat == null) {
            chat = Chat.builder()
                    .chatId(chatId)
                    .build();
            chatRepository.save(chat);
        }
        return chat;
    }
}
