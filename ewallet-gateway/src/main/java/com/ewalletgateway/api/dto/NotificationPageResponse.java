package com.ewalletgateway.api.dto;

import java.util.List;

public record NotificationPageResponse(
        List<NotificationItemResponse> items,
        String nextCursor,
        boolean hasMore
) {
}
