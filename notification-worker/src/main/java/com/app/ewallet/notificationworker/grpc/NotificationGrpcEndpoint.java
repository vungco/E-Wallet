package com.app.ewallet.notificationworker.grpc;

import com.app.ewallet.grpc.notification.v1.CountUnreadRequest;
import com.app.ewallet.grpc.notification.v1.CountUnreadResponse;
import com.app.ewallet.grpc.notification.v1.GetNotificationRequest;
import com.app.ewallet.grpc.notification.v1.GetNotificationResponse;
import com.app.ewallet.grpc.notification.v1.ListNotificationsRequest;
import com.app.ewallet.grpc.notification.v1.ListNotificationsResponse;
import com.app.ewallet.grpc.notification.v1.MarkAsReadRequest;
import com.app.ewallet.grpc.notification.v1.MarkAsReadResponse;
import com.app.ewallet.grpc.notification.v1.NotificationItem;
import com.app.ewallet.grpc.notification.v1.NotificationServiceGrpc;
import com.app.ewallet.notificationworker.model.Notification;
import com.app.ewallet.notificationworker.service.NotificationApplicationService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationGrpcEndpoint extends NotificationServiceGrpc.NotificationServiceImplBase {

    private final NotificationApplicationService notificationApplicationService;

    @Override
    public void listNotifications(
            ListNotificationsRequest request,
            StreamObserver<ListNotificationsResponse> responseObserver
    ) {
        Long userId = NotificationGrpcContext.USER_ID.get();
        if (userId == null) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL.withDescription("User context missing").asRuntimeException()
            );
            return;
        }
        try {
            var result = notificationApplicationService.list(
                    userId,
                    request.getFilter(),
                    request.getCursorBeforeId(),
                    request.getPageSize()
            );
            List<NotificationItem> items = new ArrayList<>();
            for (Notification n : result.items()) {
                items.add(NotificationGrpcMapper.toProto(n));
            }
            responseObserver.onNext(
                    ListNotificationsResponse.newBuilder()
                            .addAllItems(items)
                            .setNextCursor(result.nextCursor())
                            .setHasMore(result.hasMore())
                            .build()
            );
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(NotificationGrpcExceptionMapper.toStatus(e));
        }
    }

    @Override
    public void countUnread(CountUnreadRequest request, StreamObserver<CountUnreadResponse> responseObserver) {
        Long userId = NotificationGrpcContext.USER_ID.get();
        if (userId == null) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL.withDescription("User context missing").asRuntimeException()
            );
            return;
        }
        try {
            long c = notificationApplicationService.countUnread(userId);
            responseObserver.onNext(CountUnreadResponse.newBuilder().setCount(c).build());
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(NotificationGrpcExceptionMapper.toStatus(e));
        }
    }

    @Override
    public void getNotification(
            GetNotificationRequest request,
            StreamObserver<GetNotificationResponse> responseObserver
    ) {
        Long userId = NotificationGrpcContext.USER_ID.get();
        if (userId == null) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL.withDescription("User context missing").asRuntimeException()
            );
            return;
        }
        try {
            Notification n = notificationApplicationService.getAndMaybeMarkRead(
                    userId,
                    request.getId(),
                    request.getMarkRead()
            );
            responseObserver.onNext(
                    GetNotificationResponse.newBuilder()
                            .setItem(NotificationGrpcMapper.toProto(n))
                            .build()
            );
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(NotificationGrpcExceptionMapper.toStatus(e));
        }
    }

    @Override
    public void markAsRead(MarkAsReadRequest request, StreamObserver<MarkAsReadResponse> responseObserver) {
        Long userId = NotificationGrpcContext.USER_ID.get();
        if (userId == null) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL.withDescription("User context missing").asRuntimeException()
            );
            return;
        }
        try {
            boolean updated = notificationApplicationService.markAsRead(userId, request.getId());
            responseObserver.onNext(MarkAsReadResponse.newBuilder().setUpdated(updated).build());
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(NotificationGrpcExceptionMapper.toStatus(e));
        }
    }
}
