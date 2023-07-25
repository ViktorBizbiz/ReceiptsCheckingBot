package ua.bizbiz.receiptscheckingbot.bot.processor.text.message.impl;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu.StartCommand;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.message.MessageProcessor;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Role;
import ua.bizbiz.receiptscheckingbot.persistance.entity.User;
import ua.bizbiz.receiptscheckingbot.persistance.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.*;

@RequiredArgsConstructor
public class UserProcessor implements MessageProcessor {

    private final UserRepository userRepository;

    @Override
    public List<Validable> process(Chat chat, String text) {
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

    private long generateSecretCode() {
        Long secretCode = null;
        final var min = 100_000;
        while (userRepository.existsBySecretCode(secretCode) || secretCode == null || secretCode < min)
            secretCode = (long) (Math.random() * 1_000_000);
        return secretCode;
    }

    private User getUserIfExists(long id, Chat chat, List<Validable> responses) {
        final var optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            responses.add(new StartCommand(chat, NO_USER_FOUND_BY_ID).process(chat));
            return null;
        }
        return optionalUser.get();
    }
}
