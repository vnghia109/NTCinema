package vn.iostar.NT_cinema.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "room")
public class Room {
    @Id
    private String roomId;

    private Cinema cinema;

    private String roomName;

    private boolean isDelete;

}
