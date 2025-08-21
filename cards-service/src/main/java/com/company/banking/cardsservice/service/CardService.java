package com.company.banking.cardsservice.service;

import com.company.banking.cardsservice.client.AccountServiceFeignClient;
import com.company.banking.cardsservice.model.Card;
import com.company.banking.cardsservice.model.CardStatus;
import com.company.banking.cardsservice.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final AccountServiceFeignClient accountServiceFeignClient;

    @Transactional
    public Card issueNewCard(Long accountId) {
        // 1. Validate account exists
        accountServiceFeignClient.getAccountById(accountId);

        // 2. Generate new card details (mock implementation)
        Card card = Card.builder()
                .accountId(accountId)
                .cardNumber(generateRandomCardNumber())
                .expirationDate(LocalDate.now().plusYears(3))
                .cvv(generateRandomCvv())
                .status(CardStatus.INACTIVE)
                .cardType("DEBIT")
                .build();

        return cardRepository.save(card);
    }

    @Transactional
    public Card activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
        if (card.getStatus() != CardStatus.INACTIVE) {
            throw new IllegalStateException("Card is not in INACTIVE state");
        }
        card.setStatus(CardStatus.ACTIVE);
        return cardRepository.save(card);
    }

    @Transactional
    public Card blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("Card is already blocked");
        }
        card.setStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    public List<Card> getCardsByAccountId(Long accountId) {
        return cardRepository.findByAccountId(accountId);
    }

    private String generateRandomCardNumber() {
        // Generates a 16-digit number. Not a real card number.
        return "4" + String.format("%015d", ThreadLocalRandom.current().nextLong(1_000_000_000_000_000L));
    }

    private String generateRandomCvv() {
        return String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
