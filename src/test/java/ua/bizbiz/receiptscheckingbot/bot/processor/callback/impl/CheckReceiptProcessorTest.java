package ua.bizbiz.receiptscheckingbot.bot.processor.callback.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.bizbiz.receiptscheckingbot.data.CheckReceiptProcessorData;
import ua.bizbiz.receiptscheckingbot.persistance.repository.SubscriptionRepository;
import ua.bizbiz.receiptscheckingbot.util.DataHolder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.*;

@ExtendWith(MockitoExtension.class)
class CheckReceiptProcessorTest {
    
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private DataHolder dataHolder;
    @InjectMocks
    private CheckReceiptProcessor checkReceiptProcessor;
    @InjectMocks
    private CheckReceiptProcessorData checkReceiptProcessorData;
    
    @Test
    public void process_shouldReturnExpectedMessageWhenGivenAcceptAction() {
        final var subscription = checkReceiptProcessorData.getSubscription();
        final var callbackData = checkReceiptProcessorData.getCallbackData(ACCEPT);
        final var photos = checkReceiptProcessorData.getPhotos();
        when(subscriptionRepository.findById(any())).thenReturn(Optional.of(subscription));
        when(dataHolder.getPhotoMessages()).thenReturn(photos);
        final var expected = checkReceiptProcessorData.getExpectedSendMessage(RECEIPT_ACCEPTED);
        
        final var result = checkReceiptProcessor.process(subscription.getUser().getChat(),
                callbackData, new Message());

        Assertions.assertEquals(expected, result.get(0));
    }
    @Test
    public void process_shouldReturnExpectedMessageWhenGivenCancelAction() {
        final var subscription = checkReceiptProcessorData.getSubscription();
        final var callbackData = checkReceiptProcessorData.getCallbackData(CANCEL);
        final var photos = checkReceiptProcessorData.getPhotos();
        when(subscriptionRepository.findById(any())).thenReturn(Optional.of(subscription));
        when(dataHolder.getPhotoMessages()).thenReturn(photos);
        final var expected = checkReceiptProcessorData.getExpectedSendMessage(RECEIPT_DECLINED);
        
        final var result = checkReceiptProcessor.process(subscription.getUser().getChat(),
                callbackData, new Message());
        
        Assertions.assertEquals(expected, result.get(0));
    }
}
