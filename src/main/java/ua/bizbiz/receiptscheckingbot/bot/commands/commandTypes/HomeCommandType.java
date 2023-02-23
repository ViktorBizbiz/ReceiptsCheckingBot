package ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum HomeCommandType {
    START("start"),
    HOME("◀️ Назад");


    private final String name;

    public static Optional<HomeCommandType> parse(String name) {
        return Arrays.stream(values())
                .filter(command -> command.getName().equalsIgnoreCase(name))
                .findFirst();
    }
}
