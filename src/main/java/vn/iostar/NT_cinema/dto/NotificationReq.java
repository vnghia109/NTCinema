package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NotificationReq {
    private String title;
    private String message;
    private String sendTo;
    private String role;
    private List<String> userIds;
    private String type;
}
