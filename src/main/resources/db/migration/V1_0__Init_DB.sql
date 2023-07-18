create sequence chats_seq start with 3 increment by 1;
create sequence promotions_seq start with 1 increment by 1;
create sequence subscriptions_seq start with 1 increment by 1;
create sequence users_seq start with 3 increment by 1;

create table chats (
    id bigint not null,
    chat_id bigint,
    status varchar(255) check (status in ('ENTERING_SECRET_CODE',
                                          'AUTHORIZED_AS_USER',
                                          'AUTHORIZED_AS_ADMIN',
                                          'CREATING_USER',
                                          'READING_USER',
                                          'UPDATING_USER',
                                          'DELETING_USER',
                                          'GETTING_REPORT',
                                          'ADMIN_GETTING_PROMOTIONS',
                                          'ADMIN_GETTING_USERS',
                                          'CREATING_PROMOTION',
                                          'UPDATING_PROMOTION',
                                          'DELETING_PROMOTION',
                                          'SENDING_ANNOUNCEMENT',
                                          'SENDING_ANNOUNCEMENT_TO_ALL',
                                          'SENDING_ANNOUNCEMENT_TO_PERSON',
                                          'USER_GETTING_PROMOTIONS',
                                          'SENDING_RECEIPT',
                                          'SENDING_RECEIPT_PHOTO',
                                          'CHECKING_RECEIPTS')),
    primary key (id)
);

create table promotions (
    id bigint not null,
    name varchar(255),
    min_quantity integer,
    completion_bonus integer,
    resale_bonus integer,
    primary key (id)
);

create table subscriptions (
    id bigint not null,
    user_id bigint,
    promotion_id bigint,
    current_quantity integer,
    current_bonus integer,
    primary key (id)
);

create table users (
    id bigint not null,
    chat_id bigint unique,
    phone_number varchar(255),
    registered_at timestamp(6),
    role varchar(255) check (role in ('ADMIN','USER')),
    full_name varchar(255),
    address varchar(255),
    pharmacy_chain varchar(255),
    city_of_pharmacy varchar(255),
    secret_code bigint,
    primary key (id)
);

alter table if exists subscriptions
    add constraint subscriptions_promotions_fk
    foreign key (promotion_id) references promotions;

alter table if exists subscriptions
    add constraint subscriptions_users_fk
    foreign key (user_id) references users;

alter table if exists users
    add constraint users_chats_fk
    foreign key (chat_id) references chats;