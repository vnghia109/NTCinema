package vn.iostar.NT_cinema.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "notification")
public class Notification {
    @Id
    private String notificationId;
    @DBRef
    private User user;
    private String title;
    private String message;
    private Date createdAt;
    private boolean isRead;
    private String type;
    private String referenceId;
}
