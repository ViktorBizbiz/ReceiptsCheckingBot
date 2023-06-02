package ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes.crud;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum PromotionCrudCommandType {

    CREATE_PROMOTION("Створити акцію ➕"),
    UPDATE_PROMOTION("Змінити акцію \uD83D\uDD04"),
    DELETE_PROMOTION("Видалити акцію ❌");


    private final String name;

    public static Optional<PromotionCrudCommandType> parse(String name) {
        return Arrays.stream(values())
                .filter(command -> command.getName().equalsIgnoreCase(name))
                .findFirst();
    }
}
