![Logo](img/ReceiptsCheckingBotLogo.png)

# ReceiptsCheckingBot 🤖

Welcome to the ReceiptsCheckingBot repository! 

# About the Project 🔎

**CRM system that helps managers and pharmacists communicate.**

The application solves the problem of communication between sales managers and
their clients (pharmacists and pharmacies).
The application is based on communication with the Telegrambots API.

# Application Features 🧨

### Promotions management
- The ability to create/update/delete relevant promotions;
### Users management
- The ability to add/update/delete new users;
### Reporting
- The ability to create a report in Excel format;
### Internal communication
- The ability to send a message to a specific user or all at once;
### Reminder
- A reminder to motivate users;
### Productivity control
- The ability to send fiscal checks to confirm the sold promotional product;
### Choosing promotion program
- The ability to subscribe to a specific promotion;
### Balance monitoring
- The ability to check your balance on promotions.

# Installation 🏁

To get started with the ReceiptsCheckingBot, follow these steps:

1. Create your Telegram bot:
   - Go to [BotFather](https://t.me/BotFather) and follow the steps to create bot.
2. Configure docker-compose.yml:
   - Set `telegram-bot-username` and `telegram-bot-token` variables with values that you receive in BotFather.
3. Configure Flyway migrations:
   - Open `V1_1__Add_admins.sql` file and edit it with following values:
      - `chat_id` - you can find out your chat ID in special telegram bots(e.g. [Get My ID](https://t.me/getmyid_bot) or alternatives).
      - `full_name` - your full name in bot environment.
      - `secret_code` - 6-digits password, that bot will request to get access.
4. Re-build project with `mvnw/mvnw.cmd clean install`.
5. Run `docker-compose up -d`.
6. Bot is working! Have a good user experience!😊
# Technologies Used 💻

- **Backend:** Spring (Boot, Data)
- **DevOps tools :** Docker
- **Database:** Postgres
- **Migrations:** Flyway
- **Additional Technologies:** Telegram's API (telegrambots), Excel Library (Apache POI)


# Contributing 🤝

If you would like to contribute to the development of this web application, please follow these guidelines:

1. Fork this repository.
2. Create a new branch for your feature or bug fix: `git checkout -b feature-name`
3. Make your changes and commit them: `git commit -m "Add feature-name"`
4. Push your changes to your forked repository: `git push origin feature-name`
5. Create a pull request to merge your changes into the main repository.

# Contact 📧

If you have any questions or need assistance, please contact me:

Backend: [Viktor Bizbiz](https://www.linkedin.com/in/viktor-bizbiz-70253b284/)