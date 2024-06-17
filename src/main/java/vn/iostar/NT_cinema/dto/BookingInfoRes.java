package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.iostar.NT_cinema.entity.Booking;
import vn.iostar.NT_cinema.entity.PromotionCode;
import vn.iostar.NT_cinema.entity.PromotionFixed;
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

    private List<SeatPromotion> seats;

    private List<FoodWithCount> foods;

    private BigDecimal seatTotalPrice;

    private BigDecimal foodTotalPrice;

    private BigDecimal discount;

    private BigDecimal total;

    private List<PromotionFixed> promotionFixedList;

    private PromotionCode promotionCode;

    public BookingInfoRes(Booking booking, List<PromotionFixed> promotionFixedList, List<SeatPromotion> seats) {
        this.promotionCode = booking.getPromotionCode();
        this.bookingId = booking.getBookingId();
        this.createAt = booking.getCreateAt();
        this.seats = seats;
        this.foods = booking.getFoods();
        this.seatTotalPrice = booking.getSeatTotalPrice();
        this.foodTotalPrice = booking.getFoodTotalPrice();
        this.discount = booking.getDiscount();
        this.total = booking.getTotal();
        this.promotionFixedList = promotionFixedList;
    }
}
