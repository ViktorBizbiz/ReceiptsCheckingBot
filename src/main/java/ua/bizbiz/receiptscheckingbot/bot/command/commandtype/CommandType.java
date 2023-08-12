package ua.bizbiz.receiptscheckingbot.bot.command.commandtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.crud.PromotionCrudCommandTypeMark;
import ua.bizbiz.receiptscheckingbot.bot.command.commandtype.crud.UserCrudCommandTypeMark;
import ua.bizbiz.receiptscheckingbot.persistance.entity.ChatStatus;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum CommandType {
    START("/start", HomeCommandTypeMark.class, ChatStatus.DEFAULT),
    HOME("◀️ Назад", HomeCommandTypeMark.class, ChatStatus.DEFAULT),
    // Admin commands
    ADMIN_SHOW_USERS("Користувачі \uD83D\uDC65", MainCommandTypeMark.class, ChatStatus.AUTHORIZED_AS_ADMIN),
    CREATE_REPORT("Створити звіт \uD83D\uDCC1", MainCommandTypeMark.class, ChatStatus.AUTHORIZED_AS_ADMIN),
    ADMIN_SHOW_PROMOTIONS("Акції \uD83D\uDCCB", MainCommandTypeMark.class, ChatStatus.AUTHORIZED_AS_ADMIN),
    MAKE_AN_ANNOUNCEMENT("Зробити оголошення \uD83D\uDCE3", MainCommandTypeMark.class, ChatStatus.AUTHORIZED_AS_ADMIN),
    CHECK_RECEIPTS("Перевірка чеків 🔍", MainCommandTypeMark.class, ChatStatus.AUTHORIZED_AS_ADMIN),

    // User commands
    USER_SHOW_PROMOTIONS("Актуальні акції \uD83D\uDC8A", MainCommandTypeMark.class, ChatStatus.AUTHORIZED_AS_USER),
    SEND_RECEIPT("Відправити чек \uD83E\uDDFE", MainCommandTypeMark.class, ChatStatus.AUTHORIZED_AS_USER),
    BALANCE("Баланс \uD83D\uDCB0", MainCommandTypeMark.class, ChatStatus.AUTHORIZED_AS_USER),

    TO_PERSON("Конкретному користувачу \uD83D\uDC64", AnnouncementCommandTypeMark.class ,ChatStatus.SENDING_ANNOUNCEMENT),
    TO_CHAIN("Мережі користувачів ⛓", AnnouncementCommandTypeMark.class ,ChatStatus.SENDING_ANNOUNCEMENT),
    TO_ALL("Усім \uD83D\uDC65", AnnouncementCommandTypeMark.class ,ChatStatus.SENDING_ANNOUNCEMENT),

    CREATE_USER("Створити користувача ➕", UserCrudCommandTypeMark.class, ChatStatus.ADMIN_GETTING_USERS),
    READ_USER("Подробиці за користувачем \uD83E\uDEAA", UserCrudCommandTypeMark.class, ChatStatus.ADMIN_GETTING_USERS),
    UPDATE_USER("Змінити користувача \uD83D\uDD04", UserCrudCommandTypeMark.class, ChatStatus.ADMIN_GETTING_USERS),
    DELETE_USER("Видалити користувача ❌", UserCrudCommandTypeMark.class, ChatStatus.ADMIN_GETTING_USERS),

    CREATE_PROMOTION("Створити акцію ➕", PromotionCrudCommandTypeMark.class, ChatStatus.ADMIN_GETTING_PROMOTIONS),
    UPDATE_PROMOTION("Змінити акцію \uD83D\uDD04", PromotionCrudCommandTypeMark.class, ChatStatus.ADMIN_GETTING_PROMOTIONS),
    DELETE_PROMOTION("Видалити акцію ❌", PromotionCrudCommandTypeMark.class, ChatStatus.ADMIN_GETTING_PROMOTIONS);


    private final String name;
    private final Class<? extends Markable> classMarkable;
    private final ChatStatus status;

    public static Optional<CommandType> parse(String name) {
        return Arrays.stream(values())
                .filter(command -> command.getName().equalsIgnoreCase(name))
                .findFirst();
    }
}
