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
@Document(collection = "cinemaFinanceStats")
public class CinemaFinanceStats {
    @Id
    private String cinemaFinanceStatsId;
    private LocalDate month;
    @DBRef
    private Cinema cinema;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal ticketRevenue = BigDecimal.ZERO;
    private BigDecimal foodRevenue = BigDecimal.ZERO;
    private Integer totalOfBooking = 0;
    private BigDecimal totalExpense = BigDecimal.ZERO;
    private BigDecimal foodExpense = BigDecimal.ZERO;
    private BigDecimal otherExpense = BigDecimal.ZERO;
    private Integer totalOfOrder = 0;
    private BigDecimal profit = totalRevenue.subtract(totalExpense);

    public CinemaFinanceStats(LocalDate localDate, Cinema cinema) {
        this.month = localDate;
        this.cinema = cinema;
    }

    public void calculateProfit() {
        profit = totalRevenue.subtract(totalExpense);
    }

    public CinemaFinanceStats(LocalDate month, Cinema cinema,
                              BigDecimal totalRevenue, BigDecimal ticketRevenue,
                              BigDecimal foodRevenue, Integer totalOfBooking, BigDecimal otherExpense) {
        this.month = month;
        this.cinema = cinema;
        this.totalRevenue = totalRevenue;
        this.ticketRevenue = ticketRevenue;
        this.foodRevenue = foodRevenue;
        this.totalOfBooking = totalOfBooking;
        this.otherExpense = otherExpense;
    }
    public CinemaFinanceStats(LocalDate month, Cinema cinema,
                              BigDecimal totalExpense, BigDecimal foodExpense,
                              Integer totalOfOrder){
        this.month = month;
        this.cinema = cinema;
        this.totalExpense = totalExpense;
        this.foodExpense = foodExpense;
        this.totalOfOrder = totalOfOrder;
    }
}
