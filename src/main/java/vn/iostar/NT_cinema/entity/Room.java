package vn.iostar.NT_cinema.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "room")
public class Room {
    @Id
    private String roomId;

    @DBRef
    private Cinema cinema;

    private String roomName;

    private int rowSeat;

    private int colSeat;

    private boolean isDelete;

    private Date CreateAt;

    private Date UpdateAt;
}
