package com.company.banking.notificationservice.grpc;

import com.company.banking.grpc.notification.NotificationRequest;
import com.company.banking.grpc.notification.NotificationServiceGrpc;
import com.company.banking.notificationservice.service.NotificationService;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class NotificationGrpcService extends NotificationServiceGrpc.NotificationServiceImplBase {

    private final NotificationService notificationService;

    @Override
    public void sendNotification(NotificationRequest request, StreamObserver<Empty> responseObserver) {
        // The DTO and the protobuf message are identical, so we can't create a mapper.
        // We'll have to manually create the DTO.
        var dto = new com.company.banking.notificationservice.dto.NotificationRequest(
                request.getTo(),
                request.getSubject(),
                request.getBody()
        );
        notificationService.sendNotification(dto);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
