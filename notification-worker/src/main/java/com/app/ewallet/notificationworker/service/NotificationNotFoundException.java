package com.app.ewallet.notificationworker.service;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(long id) {
        super("Notification not found: " + id);
    }
}
