package ua.bizbiz.receiptscheckingbot.persistance.entity;

public enum ChatStatus {
    ENTERING_SECRET_CODE,
    AUTHORIZED_AS_USER,
    AUTHORIZED_AS_ADMIN,
    CREATING_NEW_USER,
    GETTING_REPORT,
    GETTING_PROMOTIONS,
    CREATING_PROMOTION,
    UPDATING_PROMOTION,
    DELETING_PROMOTION,
    SENDING_ANNOUNCEMENT,
    SENDING_ANNOUNCEMENT_TO_ALL,
    SENDING_ANNOUNCEMENT_TO_PERSON,
}
