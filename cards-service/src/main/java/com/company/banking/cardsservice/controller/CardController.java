package com.company.banking.cardsservice.controller;

import com.company.banking.cardsservice.model.Card;
import com.company.banking.cardsservice.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping("/account/{accountId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Card issueNewCard(@PathVariable Long accountId) {
        return cardService.issueNewCard(accountId);
    }

    @PutMapping("/{cardId}/activate")
    public Card activateCard(@PathVariable Long cardId) {
        return cardService.activateCard(cardId);
    }

    @PutMapping("/{cardId}/block")
    public Card blockCard(@PathVariable Long cardId) {
        return cardService.blockCard(cardId);
    }

    @GetMapping("/account/{accountId}")
    public List<Card> getCardsByAccountId(@PathVariable Long accountId) {
        return cardService.getCardsByAccountId(accountId);
    }
}
