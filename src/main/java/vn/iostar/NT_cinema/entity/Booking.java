package vn.iostar.NT_cinema.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.iostar.NT_cinema.constant.TicketStatus;
import vn.iostar.NT_cinema.dto.FoodWithCount;

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

    private List<Promotion> promotions;

    private int total;

    private TicketStatus ticketStatus;
}
