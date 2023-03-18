package ua.bizbiz.receiptscheckingbot.bot.commands.commandTypes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum MainCommandType {
    // Admin commands
    ADD_NEW_USER("Додати нового користувача \uD83D\uDC64"),
    CREATE_REPORT("Створити звіт \uD83D\uDCC1"),
    ADMIN_SHOW_PROMOTIONS("Показати акції \uD83D\uDCCB"),
    MAKE_AN_ANNOUNCEMENT("Зробити оголошення \uD83D\uDCE3"),
    CHECK_RECEIPTS("Перевірка чеків 🔍"),

    // User commands
    USER_SHOW_PROMOTIONS("Актуальні акції \uD83D\uDC8A"),
    SEND_RECEIPT("Відправити чек \uD83E\uDDFE"),
    BALANCE("Баланс \uD83D\uDCB0");

    private final String name;

    public static Optional<MainCommandType> parse(String name) {
        return Arrays.stream(values())
                .filter(command -> command.getName().equalsIgnoreCase(name))
                .findFirst();
    }
}
