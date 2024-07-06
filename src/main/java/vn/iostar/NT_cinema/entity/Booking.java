package vn.iostar.NT_cinema.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.iostar.NT_cinema.constant.TicketStatus;
import vn.iostar.NT_cinema.dto.FoodWithCount;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "booking")
public class Booking {
    @Id
    private String bookingId;

    private String userId;

    private String showtimeId;

    private Date createAt;

    private boolean isPayment = false;

    private List<Seat> seats;

    private List<FoodWithCount> foods;

    private String cinemaId;

    private BigDecimal discount = BigDecimal.ZERO;

    @DBRef
    private PromotionCode promotionCode;

    @DBRef
    private List<PromotionFixed> promotionFixeds;

    private BigDecimal seatTotalPrice;

    private BigDecimal foodTotalPrice;

    private BigDecimal total;

    private TicketStatus ticketStatus;

    private LocalDateTime cancelTime;
}
