insert into chats (id, chat_id) values (1, 770821020);
-- insert into chats (id, chat_id) values (2, 511002485);

insert into users (user_id, full_name, role, chat_id, secret_code) values (1, 'Кралін Микита Вадимович', 'ADMIN', 1, 123456);
insert into users (user_id, full_name, role, chat_id, secret_code) values (2, 'Бізбіз Віктор Геннадійович', 'ADMIN', null, 654321);

alter sequence chats_seq restart with 52;
