package ua.bizbiz.receiptscheckingbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ReceiptsCheckingBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReceiptsCheckingBotApplication.class, args);
    }

}
