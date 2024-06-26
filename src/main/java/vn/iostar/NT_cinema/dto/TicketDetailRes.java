package vn.iostar.NT_cinema.dto;

import lombok.*;
import vn.iostar.NT_cinema.constant.TicketStatus;
import vn.iostar.NT_cinema.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDetailRes {
    private String bookingId;
    private String movieId;
    private String userName;
    private String fullName;
    private String movieName;
    private LocalDate date;
    private String startTime;
    private String endTime;
    private int duration;
    private String cinemaName;
    private String roomName;
    private List<SeatBookedRes> seats;
    private List<String> foods;
    private List<PromotionFixed> promotionFixeds;
    private PromotionCode promotionCode;
    private BigDecimal price;
    private TicketStatus status;
    private Date createAt;
    private LocalDateTime cancelTime;

    public TicketDetailRes(Booking booking, User user) {
        this.bookingId = booking.getBookingId();
        this.movieId = booking.getSeats().get(0).getShowTime().getMovie().getMovieId();
        this.movieName = booking.getSeats().get(0).getShowTime().getMovie().getTitle();
        this.date = booking.getSeats().get(0).getSchedule().getDate();
        this.startTime = booking.getSeats().get(0).getSchedule().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        this.endTime = booking.getSeats().get(0).getSchedule().getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        this.duration = Integer.parseInt(booking.getSeats().get(0).getShowTime().getMovie().getDuration());
        this.cinemaName = booking.getSeats().get(0).getShowTime().getRoom().getCinema().getCinemaName();
        this.roomName = booking.getSeats().get(0).getShowTime().getRoom().getRoomName();
        this.seats = booking.getSeats().stream().map(SeatBookedRes::new).toList();
        this.foods = booking.getFoods().stream().map(FoodWithCount::getFood).map(Food::getName).collect(Collectors.toList());
        this.promotionFixeds = booking.getPromotionFixeds();
        this.promotionCode = booking.getPromotionCode();
        this.price = booking.getTotal();
        this.status = booking.getTicketStatus();
        this.createAt = booking.getCreateAt();
        this.cancelTime = booking.getCancelTime();
        this.userName = user != null ? user.getUserName() : null;
        this.fullName = user != null ? user.getFullName() : null;
    }
}
