package vn.iostar.NT_cinema.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "seat")
public class Seat {
    @Id
    private String seatId;

    private String showTimeId;

    private Price price;

    private String row;
    private String column;
    private boolean status;
//
//    @DBRef
//    private Booking booking;
}
