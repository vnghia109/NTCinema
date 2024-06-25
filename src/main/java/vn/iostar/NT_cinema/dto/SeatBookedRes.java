package vn.iostar.NT_cinema.dto;

import lombok.*;
import vn.iostar.NT_cinema.entity.Seat;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SeatBookedRes {
    private String row;
    private String column;

    public SeatBookedRes(Seat seat) {
        this.row = seat.getRow();
        this.column = seat.getColumn();
    }
}
