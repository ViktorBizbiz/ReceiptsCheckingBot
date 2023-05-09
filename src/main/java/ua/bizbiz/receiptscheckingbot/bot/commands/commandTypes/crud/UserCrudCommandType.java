package ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.crud;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum UserCrudCommandType {

    CREATE_USER("Створити користувача ➕"),
    READ_USER("Подробиці за користувачем \uD83E\uDEAA"),
    UPDATE_USER("Змінити користувача \uD83D\uDD04"),
    DELETE_USER("Видалити користувача ❌");


    private final String name;

    public static Optional<UserCrudCommandType> parse(String name) {
        return Arrays.stream(values())
                .filter(command -> command.getName().equalsIgnoreCase(name))
                .findFirst();
    }
}
