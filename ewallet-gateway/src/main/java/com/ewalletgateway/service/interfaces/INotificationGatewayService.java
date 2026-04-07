package com.ewalletgateway.service.interfaces;

import com.ewalletgateway.api.dto.MarkReadResultResponse;
import com.ewalletgateway.api.dto.NotificationItemResponse;
import com.ewalletgateway.api.dto.NotificationPageResponse;
import com.ewalletgateway.api.dto.UnreadCountResponse;

public interface INotificationGatewayService {

    NotificationPageResponse listNotifications(
            String authorizationHeader,
            String filter,
            long cursorBeforeId,
            int pageSize
    );

    UnreadCountResponse countUnread(String authorizationHeader);

    NotificationItemResponse getNotification(String authorizationHeader, long id, boolean markRead);

    MarkReadResultResponse markAsRead(String authorizationHeader, long id);
}
