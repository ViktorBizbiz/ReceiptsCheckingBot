insert into chats (id, chat_id, status) values (1, 770821020, 'AUTHORIZED_AS_ADMIN');
insert into chats (id, chat_id, status) values (2, 511002485, 'AUTHORIZED_AS_USER');
insert into chats (id, chat_id, status) values (3, 1676448884, 'AUTHORIZED_AS_USER');

insert into promotions (id, name, min_quantity, completion_bonus, resale_bonus) values (1, '☀️НАТ-Д3 5000', 15, 1500, 100);
insert into promotions (id, name, min_quantity, completion_bonus, resale_bonus) values (2, '🫁Юджика Бронхо', 30, 6000, 200);
insert into promotions (id, name, min_quantity, completion_bonus, resale_bonus) values (3, '🧡Ливолин', 10, 2500, 250);

insert into users (id, full_name, role, chat_id, secret_code) values (1, 'Кралін Микита Вадимович', 'ADMIN', 1, 123456);
insert into users (id, full_name, role, chat_id, secret_code) values (2, 'Бізбіз Віктор Геннадійович', 'USER', 2, 654321);
insert into users (id, full_name, role, chat_id, secret_code) values (3, 'Бізбіз Євгенія Петрівна', 'USER', 3, 333333);

alter sequence chats_seq restart with 53;
alter sequence promotions_seq restart with 53;
alter sequence users_seq restart with 53;
