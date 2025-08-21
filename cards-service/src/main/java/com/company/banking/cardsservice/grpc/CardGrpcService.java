package com.company.banking.cardsservice.grpc;

import com.company.banking.cardsservice.model.Card;
import com.company.banking.cardsservice.service.CardService;
import com.company.banking.grpc.card.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class CardGrpcService extends CardServiceGrpc.CardServiceImplBase {

    private final CardService cardService;

    @Override
    public void issueNewCard(IssueNewCardRequest request, StreamObserver<CardResponse> responseObserver) {
        Card card = cardService.issueNewCard(request.getAccountId());
        responseObserver.onNext(toCardResponse(card));
        responseObserver.onCompleted();
    }

    @Override
    public void activateCard(CardActionRequest request, StreamObserver<CardResponse> responseObserver) {
        Card card = cardService.activateCard(request.getCardId());
        responseObserver.onNext(toCardResponse(card));
        responseObserver.onCompleted();
    }

    @Override
    public void blockCard(CardActionRequest request, StreamObserver<CardResponse> responseObserver) {
        Card card = cardService.blockCard(request.getCardId());
        responseObserver.onNext(toCardResponse(card));
        responseObserver.onCompleted();
    }

    @Override
    public void getCardsByAccountId(GetCardsByAccountIdRequest request, StreamObserver<CardListResponse> responseObserver) {
        List<Card> cards = cardService.getCardsByAccountId(request.getAccountId());
        List<CardResponse> responses = cards.stream().map(this::toCardResponse).collect(Collectors.toList());
        CardListResponse response = CardListResponse.newBuilder().addAllCards(responses).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private CardResponse toCardResponse(Card card) {
        return CardResponse.newBuilder()
                .setId(card.getId())
                .setAccountId(card.getAccountId())
                .setCardNumber(card.getCardNumber())
                .setCardType(card.getCardType())
                .setExpirationDate(card.getExpirationDate().toString())
                .setCvv(card.getCvv())
                .setStatus(CardStatus.valueOf(card.getStatus().name()))
                .build();
    }
}
