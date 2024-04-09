package vn.iostar.NT_cinema.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.iostar.NT_cinema.constant.TicketStatus;

import java.time.LocalDate;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "ticket")
public class Ticket {
    @Id
    private String ticketId;

    private String userId;

    private String cinemaName;

    private String cinemaAddress;

    private Date createAt;

    private String movieName;

    private LocalDate date;

    private String startTime;

    private String duration;

    private String seat;

    private int ticketPrice;

    private TicketStatus ticketStatus;
}
