package vn.iostar.NT_cinema.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "showTime")
public class ShowTime {
    @Id
    private String showTimeId;

    private Room room;

    private Movie movie;

    private Date time;

    private boolean isSpecial;

    @DBRef
    private List<Seat> seats;
}
