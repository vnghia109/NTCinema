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
    private BigDecimal totalRevenue;
    private BigDecimal ticketRevenue;
    private BigDecimal foodRevenue;
    private BigDecimal totalExpense;
    private BigDecimal foodExpense;
    private BigDecimal otherExpense;
    private int totalOfOrder;
    private BigDecimal profit;
}
