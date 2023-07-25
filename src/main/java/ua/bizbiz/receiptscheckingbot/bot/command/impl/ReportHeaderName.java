package ua.bizbiz.receiptscheckingbot.bot.command.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportHeaderName {

    USER_NUMBER("№"),
    MANAGER_FULL_NAME("ФIО МП"),
    CITY_OF_PHARMACY("Місто аптеки"),
    PHARMACY_CHAIN_NAME("Назва Аптечної Мережі"),
    PHARMACY_ADDRESS("Адреса аптеки"),
    USER_FULL_NAME("ФIО провізора\\зав.аптекою"),
    USER_PHONE_NUMBER("Телефон провізора\\зав.аптекою (хто буде отримувати розрахунок)"),
    NUMBER_OF_PACKAGES_SOLD("Кількість проданих уп."),
    BONUS_FOR_SALES("Бонус (грн) за продаж");

    private final String name;
}
