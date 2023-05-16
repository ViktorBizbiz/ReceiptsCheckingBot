package ua.bizbiz.receiptscheckingbot.util;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ua.bizbiz.receiptscheckingbot.persistance.repository.SubscriptionRepository;

@Component
@RequiredArgsConstructor
public class SubscriptionsCleaner {

    private final SubscriptionRepository subscriptionRepository;

    @Scheduled(cron = "@monthly") //"0 0 0 1 * *"
    public void clear() {
        subscriptionRepository.deleteAll();
    }
}
