package ua.bizbiz.receiptscheckingbot.util;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class PhotoMessageData {

    private Integer messageId;

    private Long chatId;

    private LocalDateTime creationTime;
}
