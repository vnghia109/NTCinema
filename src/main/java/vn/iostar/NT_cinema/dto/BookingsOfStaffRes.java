package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.iostar.NT_cinema.entity.Booking;
import vn.iostar.NT_cinema.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    public BookingsOfStaffRes(Booking booking, User user) {
        this.bookingId = booking.getBookingId();
        this.userName = user != null ? user.getUserName() : null;
        this.fullName = user != null ? user.getFullName() : null;
        this.movieId = booking.getSeats().get(0).getShowTime().getMovie().getMovieId();
        this.movieName = booking.getSeats().get(0).getShowTime().getMovie().getTitle();
        this.cinemaName = booking.getSeats().get(0).getShowTime().getRoom().getCinema().getCinemaName();
        this.date = booking.getSeats().get(0).getSchedule().getDate();
        this.startTime = booking.getSeats().get(0).getSchedule().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        this.price = booking.getTotal();
        this.createAt = booking.getCreateAt();
    }
}
