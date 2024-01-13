package ua.bizbiz.receiptscheckingbot.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@PropertySource(value = "application.yaml")
public class BotConfig {

    @Value("${bot.username}")
    private String botName;

    @Value("${bot.token}")
    private String token;
}
