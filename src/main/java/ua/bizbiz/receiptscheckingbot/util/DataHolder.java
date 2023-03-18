package ua.bizbiz.receiptscheckingbot.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Getter
@Setter
public class DataHolder {
    private String subscriptionId;

    private List<PhotoMessageData> photoMessages = new ArrayList<>();

    private LocalDateTime photoCreationTime;
}
