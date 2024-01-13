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

1. Open a Terminal or Command Prompt:
    - If you're on Windows, you can use Command Prompt or PowerShell.
    - On Linux or macOS, use the Terminal.

2. Pull the application from GitHub.

3. Configure docker-compose.yml.

4. Configure Flyway migrations.

5. Navigate to the Directory:
    - Use the cd command to navigate to the directory where you saved the application.
```bash
cd /path/to/your/application-directory
```

6. Run Docker Compose:
    - Execute the following command to start the Docker Compose process. Replace docker-compose.yml with the actual filename if it's different.
```bash
docker-compose up
```

7. Wait for Completion:
    - Docker will download the necessary images and start the containers. This might take some time depending on your internet connection.

8. Access Your Application:
    - Once the process completes, you should see output indicating that your application is running. Access it through your Telegram account.

9. Stopping the Containers:
    - To stop the running containers, open a new terminal window, navigate to the same directory, and run:
```bash
docker-compose down
```

10. This stops and removes the containers.

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