package vn.iostar.NT_cinema.dto;

import lombok.*;
import vn.iostar.NT_cinema.constant.TicketStatus;
import vn.iostar.NT_cinema.entity.PromotionCode;
import vn.iostar.NT_cinema.entity.PromotionFixed;
import vn.iostar.NT_cinema.entity.Seat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

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
}
