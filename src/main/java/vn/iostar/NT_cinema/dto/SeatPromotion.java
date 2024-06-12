package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SeatPromotion {
    private String seatId;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private String row;
    private String column;
}
