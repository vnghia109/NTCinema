package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.iostar.NT_cinema.constant.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingsOfStaffRes {
    private String bookingId;
    private String userName;
    private String fullName;
    private String movieId;
    private String movieName;
    private String cinemaName;
    private LocalDate date;
    private String startTime;
    private BigDecimal price;
    private Date createAt;
}
