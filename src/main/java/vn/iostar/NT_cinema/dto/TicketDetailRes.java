package vn.iostar.NT_cinema.dto;

import lombok.*;
import vn.iostar.NT_cinema.entity.Seat;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDetailRes {
    private String movieName;
    private Date timeShow;
    private int duration;
    private String cinemaName;
    private String roomName;
    private List<Seat> seats;
    private List<FoodWithCount> foods;
}
