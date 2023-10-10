package vn.iostar.NT_cinema.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.w3c.dom.stylesheets.LinkStyle;

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

    @DBRef
    private List<Seat> seats;
}
