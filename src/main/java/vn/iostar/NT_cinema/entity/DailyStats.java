package vn.iostar.NT_cinema.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "dailyStats")
public class DailyStats {
    @Id
    private String dailyStatsId;
    private LocalDate date;
    @DBRef
    private Cinema cinema;
    private BigDecimal revenue;
    private int totalOfTickets;
    private int totalOfBookings;

    public DailyStats(LocalDate localDate, Cinema cinema, BigDecimal revenue, int totalOfTickets, int totalOfBookings) {
        this.date = localDate;
        this.cinema = cinema;
        this.revenue = revenue;
        this.totalOfTickets = totalOfTickets;
        this.totalOfBookings = totalOfBookings;
    }
}
