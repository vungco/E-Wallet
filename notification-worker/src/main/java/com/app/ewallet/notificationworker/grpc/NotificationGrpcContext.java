package com.app.ewallet.notificationworker.grpc;

import io.grpc.Context;

public final class NotificationGrpcContext {

    public static final Context.Key<Long> USER_ID = Context.key("notificationGrpcUserId");

    private NotificationGrpcContext() {
    }
}
