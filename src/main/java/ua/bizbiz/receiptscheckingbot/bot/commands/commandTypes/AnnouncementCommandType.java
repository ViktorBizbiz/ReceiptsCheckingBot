package ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum AnnouncementCommandType {

    TO_PERSON("Конкретному користувачу \uD83D\uDC64"),
    TO_ALL("Усім \uD83D\uDC65");


    private final String name;

    public static Optional<AnnouncementCommandType> parse(String name) {
        return Arrays.stream(values())
                .filter(command -> command.getName().equalsIgnoreCase(name))
                .findFirst();
    }
}
