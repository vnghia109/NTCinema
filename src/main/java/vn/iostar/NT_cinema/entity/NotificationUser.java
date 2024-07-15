package vn.iostar.NT_cinema.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.ZoneId;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "notificationUser")
public class NotificationUser {
    @Id
    private String notificationUserId;
    @DBRef
    private User user;
    @DBRef
    private Notification notification;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NotificationUser(User user, Notification notification) {
        this.user = user;
        this.notification = notification;
        this.read = false;
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        this.createdAt = LocalDateTime.now(zoneId);
        this.updatedAt = LocalDateTime.now(zoneId);
    }
}
