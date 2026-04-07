package com.app.ewallet.notificationworker.grpc;

import com.app.ewallet.grpc.notification.v1.NotificationItem;
import com.app.ewallet.notificationworker.model.Notification;

public final class NotificationGrpcMapper {

    private NotificationGrpcMapper() {
    }

    public static NotificationItem toProto(Notification n) {
        NotificationItem.Builder b = NotificationItem.newBuilder()
                .setId(n.getId())
                .setTitle(n.getTitle())
                .setBody(n.getBody())
                .setTransferStatus(n.getTransferStatus().name())
                .setRead(n.isReadFlag())
                .setRequestId(n.getRequestId())
                .setAmount(n.getAmount().toPlainString())
                .setCounterpartUserId(n.getCounterpartUserId())
                .setUserRole(n.getUserRole().name())
                .setCreatedAt(n.getCreatedAt().toString());
        if (n.getTransactionId() != null) {
            b.setTransactionId(n.getTransactionId());
        }
        return b.build();
    }
}
