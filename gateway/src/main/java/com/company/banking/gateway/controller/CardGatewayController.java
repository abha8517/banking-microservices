package com.company.banking.gateway.controller;

import com.company.banking.grpc.card.*;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardGatewayController {

    @GrpcClient("cards-service")
    private CardServiceGrpc.CardServiceBlockingStub cardServiceBlockingStub;

    @PostMapping("/account/{accountId}")
    public CardResponse issueNewCard(@PathVariable Long accountId) {
        return cardServiceBlockingStub.issueNewCard(
                IssueNewCardRequest.newBuilder().setAccountId(accountId).build()
        );
    }

    @PutMapping("/{cardId}/activate")
    public CardResponse activateCard(@PathVariable Long cardId) {
        return cardServiceBlockingStub.activateCard(
                CardActionRequest.newBuilder().setCardId(cardId).build()
        );
    }

    @PutMapping("/{cardId}/block")
    public CardResponse blockCard(@PathVariable Long cardId) {
        return cardServiceBlockingStub.blockCard(
                CardActionRequest.newBuilder().setCardId(cardId).build()
        );
    }

    @GetMapping("/account/{accountId}")
    public CardListResponse getCardsByAccountId(@PathVariable Long accountId) {
        return cardServiceBlockingStub.getCardsByAccountId(
                GetCardsByAccountIdRequest.newBuilder().setAccountId(accountId).build()
        );
    }
}
