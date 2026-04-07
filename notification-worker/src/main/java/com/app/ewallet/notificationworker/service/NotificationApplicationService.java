package com.app.ewallet.notificationworker.service;

import com.app.ewallet.grpc.notification.v1.NotificationFilter;
import com.app.ewallet.notificationworker.model.Notification;
import com.app.ewallet.notificationworker.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationApplicationService {

    private final NotificationRepository notificationRepository;

    public ListNotificationsResult list(
            long userId,
            NotificationFilter filter,
            long cursorBeforeId,
            int pageSize
    ) {
        if (filter == NotificationFilter.NOTIFICATION_FILTER_UNSPECIFIED) {
            throw new IllegalArgumentException("filter is required (UNREAD or READ)");
        }
        boolean readFlag = filter == NotificationFilter.READ;
        int size = pageSize <= 0 ? 20 : Math.min(pageSize, 100);
        Long beforeId = cursorBeforeId <= 0 ? null : cursorBeforeId;
        var pageable = PageRequest.of(0, size + 1);
        List<Notification> rows = notificationRepository.findPageForUser(userId, readFlag, beforeId, pageable);
        boolean hasMore = rows.size() > size;
        List<Notification> page = hasMore ? new ArrayList<>(rows.subList(0, size)) : rows;
        String nextCursor = "";
        if (hasMore && !page.isEmpty()) {
            nextCursor = String.valueOf(page.get(page.size() - 1).getId());
        }
        return new ListNotificationsResult(page, nextCursor, hasMore);
    }

    public long countUnread(long userId) {
        return notificationRepository.countByUserIdAndReadFlagIsFalse(userId);
    }

    @Transactional
    public Notification getAndMaybeMarkRead(long userId, long id, boolean markRead) {
        Notification n = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        if (markRead && !n.isReadFlag()) {
            n.setReadFlag(true);
            notificationRepository.save(n);
        }
        return n;
    }

    @Transactional
    public boolean markAsRead(long userId, long id) {
        Notification n = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        if (n.isReadFlag()) {
            return false;
        }
        n.setReadFlag(true);
        notificationRepository.save(n);
        return true;
    }

    public record ListNotificationsResult(List<Notification> items, String nextCursor, boolean hasMore) {
    }
}
