package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StockEntryReq {
    private String foodId;
    private int quantity;
    private int purchasePrice;
    private String supplier;
    private int totalPrice;
}
