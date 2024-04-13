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
@Document(collection = "seat")
public class Seat {
    @Id
    private String seatId;

    private String showTimeId;

    @DBRef
    private Schedule schedule;

    @DBRef
    private Price price;

    private String row;
    private String column;
    private boolean status;

    public String convertToUnicode() {
        int num = Integer.parseInt(getRow());
        if (num < 1 || num > 26) {
            return "Out of range";
        }
        char unicodeChar = (char) ('A' + num - 1);
        return String.valueOf(unicodeChar);
    }
}
