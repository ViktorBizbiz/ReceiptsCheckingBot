package ua.bizbiz.receiptscheckingbot.util;

public class ClientAnswerMessages {
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
}
