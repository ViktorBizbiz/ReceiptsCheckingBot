package ua.bizbiz.receiptscheckingbot.util;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class PhotoMessageData {

    private Integer messageId;

    private Long chatId;

    private LocalDateTime creationTime;
}
