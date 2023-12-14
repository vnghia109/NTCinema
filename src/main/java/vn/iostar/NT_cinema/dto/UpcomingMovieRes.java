package vn.iostar.NT_cinema.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcomingMovieRes {
    private String bookingId;
    private String movieName;
    private String cinemaName;
    private Date timeShow;
    private int price;
}
