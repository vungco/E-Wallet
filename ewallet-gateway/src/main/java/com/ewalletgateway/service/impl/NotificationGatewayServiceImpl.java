package com.ewalletgateway.service.impl;

import com.app.ewallet.grpc.notification.v1.CountUnreadRequest;
import com.app.ewallet.grpc.notification.v1.GetNotificationRequest;
import com.app.ewallet.grpc.notification.v1.ListNotificationsRequest;
import com.app.ewallet.grpc.notification.v1.MarkAsReadRequest;
import com.app.ewallet.grpc.notification.v1.NotificationFilter;
import com.app.ewallet.grpc.notification.v1.NotificationItem;
import com.app.ewallet.grpc.notification.v1.NotificationServiceGrpc;
import com.ewalletgateway.api.dto.MarkReadResultResponse;
import com.ewalletgateway.api.dto.NotificationItemResponse;
import com.ewalletgateway.api.dto.NotificationPageResponse;
import com.ewalletgateway.api.dto.UnreadCountResponse;
import com.ewalletgateway.service.interfaces.INotificationGatewayService;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class NotificationGatewayServiceImpl implements INotificationGatewayService {

    private static final Metadata.Key<String> AUTHORIZATION =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final ManagedChannel notificationGrpcChannel;

    @Override
    public NotificationPageResponse listNotifications(
            String authorizationHeader,
            String filter,
            long cursorBeforeId,
            int pageSize
    ) {
        NotificationFilter f = parseFilter(filter);
        var stub = stubWithAuth(authorizationHeader);
        var r = stub.listNotifications(
                ListNotificationsRequest.newBuilder()
                        .setFilter(f)
                        .setCursorBeforeId(cursorBeforeId)
                        .setPageSize(pageSize)
                        .build()
        );
        List<NotificationItemResponse> items = r.getItemsList().stream()
                .map(NotificationGatewayServiceImpl::mapItem)
                .toList();
        return new NotificationPageResponse(items, r.getNextCursor(), r.getHasMore());
    }

    @Override
    public UnreadCountResponse countUnread(String authorizationHeader) {
        var stub = stubWithAuth(authorizationHeader);
        var r = stub.countUnread(CountUnreadRequest.getDefaultInstance());
        return new UnreadCountResponse(r.getCount());
    }

    @Override
    public NotificationItemResponse getNotification(String authorizationHeader, long id, boolean markRead) {
        var stub = stubWithAuth(authorizationHeader);
        var r = stub.getNotification(
                GetNotificationRequest.newBuilder()
                        .setId(id)
                        .setMarkRead(markRead)
                        .build()
        );
        if (!r.hasItem()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found");
        }
        return mapItem(r.getItem());
    }

    @Override
    public MarkReadResultResponse markAsRead(String authorizationHeader, long id) {
        var stub = stubWithAuth(authorizationHeader);
        var r = stub.markAsRead(MarkAsReadRequest.newBuilder().setId(id).build());
        return new MarkReadResultResponse(r.getUpdated());
    }

    private static NotificationFilter parseFilter(String filter) {
        if (filter == null || filter.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "filter is required (unread or read)");
        }
        return switch (filter.trim().toLowerCase()) {
            case "unread" -> NotificationFilter.UNREAD;
            case "read" -> NotificationFilter.READ;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "filter must be unread or read");
        };
    }

    private static NotificationItemResponse mapItem(NotificationItem it) {
        long tx = it.getTransactionId();
        return new NotificationItemResponse(
                it.getId(),
                it.getTitle(),
                it.getBody(),
                it.getTransferStatus(),
                it.getRead(),
                it.getRequestId(),
                tx == 0 ? null : tx,
                it.getAmount().isEmpty() ? null : it.getAmount(),
                it.getCounterpartUserId() == 0 ? null : it.getCounterpartUserId(),
                it.getUserRole().isEmpty() ? null : it.getUserRole(),
                it.getCreatedAt().isEmpty() ? null : it.getCreatedAt()
        );
    }

    private NotificationServiceGrpc.NotificationServiceBlockingStub stubWithAuth(String authorizationHeader) {
        Metadata md = new Metadata();
        md.put(AUTHORIZATION, authorizationHeader);
        var intercepted = ClientInterceptors.intercept(
                notificationGrpcChannel,
                MetadataUtils.newAttachHeadersInterceptor(md)
        );
        return NotificationServiceGrpc.newBlockingStub(intercepted)
                .withDeadlineAfter(30, TimeUnit.SECONDS);
    }
}
