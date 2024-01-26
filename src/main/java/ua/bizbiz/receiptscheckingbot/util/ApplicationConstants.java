package ua.bizbiz.receiptscheckingbot.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApplicationConstants {

    public static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";

    @UtilityClass
    public class Emoji {

        public static final String POINT_RIGHT_EMOJI = "\uD83D\uDC49\uD83C\uDFFB ";
        public static final String POINT_DOWN_EMOJI = "\uD83D\uDC47\uD83C\uDFFB ";
        public static final String CHECK_MARK_EMOJI = "✅ ";
    }

    @UtilityClass
    public class Command {
        public static final String TELEGRAM_COMMAND_PREFIX = "/";
    }

    @UtilityClass
    public class ClientAnswerMessage {
        public static final String NO_SUBSCRIPTION_FOUND_1 = "⚠️ У вас ще немає жодної підписки.\nСпочатку підпишіться хоча б на одну акцію.";
        public static final String NO_SUBSCRIPTION_FOUND_2 = """
                Жоден користувач ще не має жодної підписки на акцію.
                    
                Чим ще я можу допомогти вам?""";
        public static final String NO_PROMOTION_FOUND = """
                Ще немає жодної акції.
                    
                Чим ще я можу допомогти вам?""";
        public static final String NO_USER_FOUND_BY_ID = """
                ⚠️ За даним ID користувачів не існує.
                        
                Чим ще я можу допомогти вам?""";
        public static final String CANNOT_DELETE_YOURSELF = """
                ⚠️ Ви не можете видалити себе.
                        
                Чим ще я можу допомогти вам?""";
        public static final String USER_DELETED_SUCCESSFULLY = """
                ✅ Користувача успішно видалено.
                        
                Чим ще я можу допомогти вам?""";
        public static final String USER_UPDATED_SUCCESSFULLY = """
                ✅ Користувача успішно змінено.
                    
                Чим ще я можу допомогти вам?""";
        public static final String USER_CREATED_SUCCESSFULLY = """
                ✅ Новий користувач був створений.
                \uD83D\uDD10 Перешліть йому цей код доступу: %d
                """;
        public static final String USER_PROFILE_DATA = """
                ID: %s
                ПІП: %s
                Адреса аптеки: %s
                Назва мережі аптек: %s
                Назва міста аптеки: %s
                Номер телефону: %s
                        
                Чим ще я можу допомогти вам?""";
        public static final String WRONG_AUTHORIZATION_CODE = "⚠️ Невірний код. \nАвторизація не пройдена. Спробуйте ще раз.";
        public static final String SUCCESSFUL_AUTHORIZATION = "✅ Авторизація пройшла успішно.";
        public static final String NO_AUTHORIZED_USER_FOUND = """
                Ще немає жодного користувача, або він ще не авторизувався у боті.
                Спочатку додайте хоча б одного та переконайтеся, що він авторизований.
                    
                Чим ще я можу допомогти вам?""";
        public static final String ID_AND_TEXT_MESSAGE_REQUEST = """
                    
                Введіть ID користувача, якому ви хочете відправити повідомлення та саме повідомлення за наступним шаблоном:
                ID
                ваше_повідомлення
                """;
        public static final String CHAIN_NAME_AND_TEXT_MESSAGE_REQUEST = """
                                
                Введіть назву мережі аптек, користувачам якої ви хочете відправити повідомлення та саме повідомлення за наступним шаблоном:
                назва_мережі_аптек
                ваше_повідомлення
                """;
        public static final String MESSAGE_SENT_SUCCESSFULLY = "✅ Повідомлення було відправлено.";
        public static final String NO_PROMOTION_FOUND_BY_ID = """
                ⚠️ За даним ID акцій не існує.
                        
                Чим ще я можу допомогти вам?""";
        public static final String PROMOTION_CREATED_SUCCESSFULLY = """
                ✅ Акцію успішно створено.
                    
                Чим ще я можу допомогти вам?""";
        public static final String PROMOTION_UPDATED_SUCCESSFULLY = """
                ✅ Акцію успішно змінено.
                                                
                Чим ще я можу допомогти вам?""";
        public static final String PROMOTION_DELETED_SUCCESSFULLY = """
                ✅ Акцію успішно видалено.
                    
                Чим ще я можу допомогти вам?""";
        public static final String PHOTO_IN_PROCESSING = "♻️ Фото чеку [%s, %s шт., станом на %s] відправлено в обробку.";
        public static final String RECEIPT_INFO = """
                Від: %s
                Назва акції: %s
                Кількість препарату для підтвердження: %s шт.
                                
                ‼️ Зверніть увагу!
                Щоб підтвердити/відхилити чек, перейдіть у режим "Перевірка чеків 🔍" у головному меню.
                """;
        public static final String FORGOT_ABOUT_DRUGS_QUANTITY = """
                ⚠️ Ви не написали під фото кількість препаратів(в штуках), яку ви хочете підтвердити цим фото.
                Фото не було відправлено.
                Спробуйте ще раз.
                """;
        public static final String RECEIPT_DECLINED = "❌ Ваш чек [%s, %s шт., станом на %s] було відхилено.";
        public static final String RECEIPT_ACCEPTED = "✅ Ваш чек [%s, %s шт., станом на %s] було підтверджено.";
        public static final String WAITING_FOR_PHOTO = """
                Чекаю на ваше фото.
                ‼️ Також допишіть разом із фото кількість препаратів(в штуках), яку ви хочете підтвердити цим фото.
                В іншому випадку вам не зарахується ця кількість препаратів.
                """;
        public static final String ACCEPT = "✅ Підтвердити";
        public static final String CANCEL = "❌ Відхилити";
        public static final String SOMETHING_WENT_WRONG = "\uD83D\uDD34 Щось пішло не так.\n\nЧим ще я можу допомогти вам?";
        public static final String WHOM_SEND_MESSAGE = "Кому треба відправити повідомлення?";
        public static final String TEXT_MESSAGE_REQUEST = "✍️ Введіть повідомлення.";
        public static final String ENTER_AUTHORIZATION_CODE = """
                Вітаю! Для подальшого користування ботом, пройдіть авторизацію.
                ✍️ Введіть секретний код доступу.
                """;
        public static final String WHAT_ELSE_HELP_YOU_NEED = "Чим ще я можу допомогти вам?";
        public static final String PROMOTION_INFO = """
                %d. %s
                Мінімальна кількість: %d уп.
                Бонус за мінімальну кількість: %d грн.
                Бонус за кожну наступну упаковку: %d грн.
                                            
                """;
        public static final String NO_PROMOTION_FOUND_2 = """
                Ще немає жодної акції.
                Спочатку додайте хоча б одну.
                """;
        public static final String ENTER_PROMOTION_DATA_REQUEST = """
                ✍️ Введіть дані в такому порядку:
                назва_акції
                мінімальна_кількість_уп
                бонус_за_мінімальну_кількість_уп
                бонус_за_кожну_наступну_уп
                """;
        public static final String ENTER_PROMOTION_ID_REQUEST = "✍️ Введіть ID акції, яку хочете видалити.";
        public static final String ENTER_PROMOTION_ID_AND_NEW_DATA_TO_UPDATE_REQUEST = """
                ✍️ Введіть ID акції, яку хочете змінити, та нові дані в такому порядку:
                ID акції
                нова_назва_акції
                нова_мінімальна_кількість_уп
                новий_бонус_за_мінімальну_кількість_уп
                новий_бонус_за_кожну_наступну_уп
                """;
        public static final String PROMOTION_INFO_2 = """
                %s
                Мінімальна кількість: %d уп.
                Бонус за мінімальну кількість: %d грн.
                Бонус за кожну наступну упаковку: %d грн.
                                
                """;
        public static final String CHOOSE_PROMOTION = "Оберіть нижче, на яку акцію ви хочете підписатися/відписатися.\n\n";
        public static final String LOSE_PROGRESS_WARNING_ON_UNSUBSCRIBE = "❗️❗️❗️ Зверніть увагу! Якщо ви відпишетеся від акції, то втратите увесь прогрес по ній.";
        public static final String ENTER_USER_DATA_TO_CREATE = """
                ✍️ Введіть дані користувача за наступним шаблоном:
                ПІП
                адреса_аптеки
                назва_мережі_аптек
                назва_міста_аптеки
                номер_телефону
                                
                ❗️ Вводьте дані уважно!
                Вони будуть відображатися у звітах у такому ж вигляді.
                """;
        public static final String ENTER_USER_DATA_TO_DELETE = "✍️ Введіть ID користувача, якого хочете видалити.";
        public static final String ENTER_USER_DATA_TO_READ = "✍️ Введіть ID користувача, подробиці якого хочете відкрити.";
        public static final String ENTER_USER_DATA_TO_UPDATE = """
                ✍️ Введіть ID користувача, якого хочете змінити, та нові дані в такому порядку:
                ID користувача
                ПІП
                адреса_аптеки
                назва_мережі_аптек
                назва_міста_аптеки
                номер_телефону
                                
                ❗️ Вводьте дані уважно!
                Вони будуть відображатися у звітах у такому ж вигляді.
                """;
        public static final String SUBSCRIPTIONS_RESULTS = "За вашими підписками у вас наступні результати:\n\n";
        public static final String YOUR_BALANCE_INFO = """
                %s
                Теперішня кількість/Мінімальна кількість: %s/%s
                Загальний бонус за підпискою: %s
                                    
                """;
        public static final String TOTAL_BALANCE = "Усього бонусів: ";
        public static final String RECEIPTS_CHECKING_MODE_IS_ENABLED = """
                Ви у режимі перевірки чеків.
                Зараз ви можете підтверджувати/відхиляти чеки.
                Щоб вийти з цього режиму, натисніть "◀️ Назад".
                """;
        public static final String REPORT_DATE_INFO = "Звіт станом на\n\uD83D\uDDD3 %d.%d.%d\n\uD83D\uDD50 %d:%d";
        public static final String CHOOSE_SUBSCRIPTION_TO_SEND_RECEIPT = "\uD83D\uDC47\uD83C\uDFFB Оберіть нижче, за якою підпискою ви хочете відправити чек.";
    }
}
