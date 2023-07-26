package ua.bizbiz.receiptscheckingbot.bot.processor.photo;

import org.springframework.stereotype.Component;
import ua.bizbiz.receiptscheckingbot.bot.processor.photo.impl.PhotoReceiptProcessor;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.ChatStatus;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PhotoProcessorFactory {

    private final Map<Class<? extends PhotoProcessor>, PhotoProcessor> photoProcessorMap;

    public PhotoProcessorFactory(List<PhotoProcessor> photoProcessors) {
        this.photoProcessorMap = photoProcessors.stream()
                .collect(Collectors.toMap(PhotoProcessor::getClass, Function.identity()));
    }

    public PhotoProcessor getPhotoProcessor(Chat chat) {

        if (chat.getStatus().equals(ChatStatus.SENDING_RECEIPT_PHOTO)) {
            return photoProcessorMap.get(PhotoReceiptProcessor.class);
        }
        return null;
    }
}
