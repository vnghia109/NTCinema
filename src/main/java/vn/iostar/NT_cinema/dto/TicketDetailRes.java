package vn.iostar.NT_cinema.dto;

import lombok.*;
import vn.iostar.NT_cinema.constant.TicketStatus;
import vn.iostar.NT_cinema.entity.Seat;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDetailRes {
    private String movieId;
    private String movieName;
    private LocalDate date;
    private String startTime;
    private String endTime;
    private int duration;
    private String cinemaName;
    private String roomName;
    private List<Seat> seats;
    private List<FoodWithCount> foods;
    private int price;
    private TicketStatus status;
}
