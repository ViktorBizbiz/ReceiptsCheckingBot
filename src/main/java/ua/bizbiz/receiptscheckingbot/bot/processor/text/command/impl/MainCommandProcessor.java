package ua.bizbiz.receiptscheckingbot.bot.processor.text.command.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import ua.bizbiz.receiptscheckingbot.bot.command.ProcessableCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.CommandType;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.MainCommandTypeMark;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.Markable;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.BalanceCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.CheckReceiptsCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.CreateReportCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.SendReceiptCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.announcement.MakeAnnouncementCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu.HomeCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu.StartCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.promotion.AdminShowPromotionsCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.promotion.UserShowPromotionsCommand;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.user.AdminShowUsersCommand;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.command.CommandProcessor;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.repository.PromotionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.SubscriptionRepository;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MainCommandProcessor implements CommandProcessor {

    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;
    private final SubscriptionRepository subscriptionRepository;
    @Override
    public List<Validable> process(Chat chat, CommandType command) {
        log.info("MainCommandType detected: " + command);
        final List<ProcessableCommand> processableCommands = new ArrayList<>();
        switch (command) {
            case ADMIN_SHOW_USERS -> {
                final var users = userRepository.findAll();
                if (!users.isEmpty()) {
                    processableCommands.add(new AdminShowUsersCommand(users));
                } else {
                    throw new RuntimeException("Something went wrong [users.size() == 0]");
                }
            }
            case CREATE_REPORT -> {
                final var reportData = subscriptionRepository.findAll();
                final var promotions = promotionRepository.findAll();
                if (!reportData.isEmpty() && !promotions.isEmpty()) {
                    processableCommands.add(new CreateReportCommand(reportData, promotions));
                    processableCommands.add(new HomeCommand(chat));
                } else {
                    processableCommands.add(new StartCommand(chat, NO_SUBSCRIPTION_FOUND_2));
                }
            }
            case ADMIN_SHOW_PROMOTIONS -> {
                final var promotions = promotionRepository.findAll();
                if (!promotions.isEmpty()) {
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
                if (!promotions.isEmpty()) {
                    processableCommands.add(new UserShowPromotionsCommand(promotions, userSubscriptions));
                } else {
                    processableCommands.add(new StartCommand(chat, NO_PROMOTION_FOUND));
                }
            }
            case SEND_RECEIPT -> {
                final var subscriptions = subscriptionRepository.findAllByUserId(chat.getUser().getId());
                if (!subscriptions.isEmpty()) {
                    processableCommands.add(new SendReceiptCommand(subscriptions));
                } else {
                    processableCommands.add(new StartCommand(chat, NO_SUBSCRIPTION_FOUND_1));
                }
            }
            case BALANCE -> {
                final var subscriptions = subscriptionRepository.findAllByUserId(chat.getUser().getId());
                if (!subscriptions.isEmpty()) {
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

    @Override
    public Class<? extends Markable> getMarkClass() {
        return MainCommandTypeMark.class;
    }
}
