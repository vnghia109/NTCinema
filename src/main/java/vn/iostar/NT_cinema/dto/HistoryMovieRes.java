package vn.iostar.NT_cinema.dto;

import lombok.*;
import org.springframework.scheduling.support.SimpleTriggerContext;
import vn.iostar.NT_cinema.constant.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryMovieRes {
    private String bookingId;
    private String movieId;
    private String movieName;
    private String cinemaName;
    private LocalDate date;
    private String startTime;
    private BigDecimal price;
    private Date createAt;
    private TicketStatus ticketStatus;
}
