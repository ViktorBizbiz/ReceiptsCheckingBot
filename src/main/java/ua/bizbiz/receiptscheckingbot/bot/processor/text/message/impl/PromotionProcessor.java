package ua.bizbiz.receiptscheckingbot.bot.processor.text.message.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import ua.bizbiz.receiptscheckingbot.bot.command.impl.mainmenu.StartCommand;
import ua.bizbiz.receiptscheckingbot.bot.processor.text.message.MessageProcessor;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Chat;
import ua.bizbiz.receiptscheckingbot.persistance.entity.Promotion;
import ua.bizbiz.receiptscheckingbot.persistance.repository.PromotionRepository;

import java.util.ArrayList;
import java.util.List;

import static ua.bizbiz.receiptscheckingbot.util.ApplicationConstants.ClientAnswerMessage.*;

@Component
@RequiredArgsConstructor
public class PromotionProcessor implements MessageProcessor {

    private final PromotionRepository promotionRepository;

    @Override
    public List<Validable> process(Chat chat, String text) {
        List<Validable> responses = new ArrayList<>();
        String[] splittedText = text.split("\n");
        switch (chat.getStatus()) {
            case CREATING_PROMOTION -> {
                final var name = splittedText[0];
                final var minQuantity = Integer.parseInt(splittedText[1]);
                final var completionBonus = Integer.parseInt(splittedText[2]);
                final var resaleBonus = Integer.parseInt(splittedText[3]);
                promotionRepository.save(Promotion.builder()
                        .name(name)
                        .minQuantity(minQuantity)
                        .completionBonus(completionBonus)
                        .resaleBonus(resaleBonus)
                        .build());
                responses.add(new StartCommand(chat, PROMOTION_CREATED_SUCCESSFULLY).process(chat));
            }
            case UPDATING_PROMOTION -> {
                final var id = Long.parseLong(splittedText[0]);
                final var name = splittedText[1];
                final var minQuantity = Integer.parseInt(splittedText[2]);
                final var completionBonus = Integer.parseInt(splittedText[3]);
                final var resaleBonus = Integer.parseInt(splittedText[4]);
                if (!promotionRepository.existsById(id)) {
                    responses.add(new StartCommand(chat, NO_PROMOTION_FOUND_BY_ID).process(chat));
                }
                promotionRepository.save(Promotion.builder()
                        .id(id)
                        .name(name)
                        .minQuantity(minQuantity)
                        .completionBonus(completionBonus)
                        .resaleBonus(resaleBonus)
                        .build());
                responses.add(new StartCommand(chat, PROMOTION_UPDATED_SUCCESSFULLY).process(chat));
            }
            case DELETING_PROMOTION -> {
                final var id = Long.parseLong(text);
                if (!promotionRepository.existsById(id)) {
                    responses.add(new StartCommand(chat, NO_PROMOTION_FOUND_BY_ID).process(chat));
                }
                promotionRepository.deleteById(id);
                responses.add(new StartCommand(chat, PROMOTION_DELETED_SUCCESSFULLY).process(chat));
            }
        }
        return responses;
    }
}
