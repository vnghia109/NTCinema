package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.iostar.NT_cinema.entity.Booking;
import vn.iostar.NT_cinema.entity.Seat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingInfoRes {
    private String bookingId;

    private Date createAt;

    private List<Seat> seats;

    private List<FoodWithCount> foods;

    private BigDecimal seatTotalPrice;

    private BigDecimal foodTotalPrice;

    private BigDecimal discount;

    private BigDecimal total;

    public BookingInfoRes(Booking booking) {
        this.bookingId = booking.getBookingId();
        this.createAt = booking.getCreateAt();
        this.seats = booking.getSeats();
        this.foods = booking.getFoods();
        this.seatTotalPrice = booking.getSeatTotalPrice();
        this.foodTotalPrice = booking.getFoodTotalPrice();
        this.discount = booking.getDiscount();
        this.total = booking.getTotal();
    }
}
