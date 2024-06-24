package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import vn.iostar.NT_cinema.entity.Notification;
import vn.iostar.NT_cinema.entity.NotificationUser;
import vn.iostar.NT_cinema.entity.User;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NotificationsRes {
    private String notificationUserId;
    private String userId;
    private Notification notification;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NotificationsRes(NotificationUser notificationUser) {
        this.notificationUserId = notificationUser.getNotificationUserId();
        this.userId = notificationUser.getUser().getUserId();
        this.notification = notificationUser.getNotification();
        this.read = notificationUser.isRead();
        this.createdAt = notificationUser.getCreatedAt();
        this.updatedAt = notificationUser.getUpdatedAt();
    }
}
