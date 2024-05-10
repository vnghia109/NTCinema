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
import java.time.Month;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "monthlyStats")
public class MonthlyStats {
    @Id
    private String monthlyStatsId;
    private LocalDate month;
    @DBRef
    private Cinema cinema;
    private BigDecimal revenue;
    private int totalOfTickets;
    private int totalOfBookings;

    public MonthlyStats(LocalDate month, Cinema cinema, BigDecimal bigDecimal, int totalOfTickets, int totalOfBookings) {
        this.month = month;
        this.cinema = cinema;
        this.revenue = bigDecimal;
        this.totalOfTickets = totalOfTickets;
        this.totalOfBookings = totalOfBookings;
    }
}
