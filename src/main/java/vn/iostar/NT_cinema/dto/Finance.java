package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Finance {
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal ticketRevenue = BigDecimal.ZERO;
    private BigDecimal foodRevenue = BigDecimal.ZERO;
    private int totalOfBooking = 0;
    private BigDecimal totalExpense = BigDecimal.ZERO;
    private BigDecimal foodExpense = BigDecimal.ZERO;
    private BigDecimal otherExpense = BigDecimal.ZERO;
    private int totalOfOrder = 0;
    private BigDecimal profit = BigDecimal.ZERO;
}
