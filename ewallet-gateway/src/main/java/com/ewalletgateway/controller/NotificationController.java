package com.ewalletgateway.controller;

import com.ewalletgateway.api.dto.MarkReadResultResponse;
import com.ewalletgateway.api.dto.NotificationItemResponse;
import com.ewalletgateway.api.dto.NotificationPageResponse;
import com.ewalletgateway.api.dto.UnreadCountResponse;
import com.ewalletgateway.config.OpenApiConfig;
import com.ewalletgateway.security.AuthorizationHeaderAccessor;
import com.ewalletgateway.service.interfaces.INotificationGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT_SCHEME)
public class NotificationController {

    private static final int DEFAULT_PAGE = 20;
    private static final int MAX_PAGE = 100;

    private final INotificationGatewayService notificationGatewayService;

    @GetMapping
    @Operation(summary = "Danh sách thông báo theo trạng thái (phân trang cursor)")
    public NotificationPageResponse list(
            @RequestParam String filter,
            @RequestParam(name = "cursor", required = false, defaultValue = "0") String cursor,
            @RequestParam(name = "limit", required = false) Integer limit,
            Authentication authentication
    ) {
        long cursorBefore = parseCursor(cursor);
        int pageSize = clampPageSize(limit);
        return notificationGatewayService.listNotifications(
                AuthorizationHeaderAccessor.bearerHeader(authentication),
                filter,
                cursorBefore,
                pageSize
        );
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Số thông báo chưa đọc")
    public UnreadCountResponse unreadCount(Authentication authentication) {
        return notificationGatewayService.countUnread(AuthorizationHeaderAccessor.bearerHeader(authentication));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết thông báo (tùy chọn đánh dấu đã đọc khi markRead=true)")
    public NotificationItemResponse getOne(
            @PathVariable long id,
            @RequestParam(name = "markRead", required = false, defaultValue = "false") boolean markRead,
            Authentication authentication
    ) {
        return notificationGatewayService.getNotification(
                AuthorizationHeaderAccessor.bearerHeader(authentication),
                id,
                markRead
        );
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Đánh dấu đã đọc")
    public MarkReadResultResponse markRead(@PathVariable long id, Authentication authentication) {
        return notificationGatewayService.markAsRead(
                AuthorizationHeaderAccessor.bearerHeader(authentication),
                id
        );
    }

    private static long parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(cursor.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static int clampPageSize(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_PAGE;
        }
        return Math.min(limit, MAX_PAGE);
    }
}
