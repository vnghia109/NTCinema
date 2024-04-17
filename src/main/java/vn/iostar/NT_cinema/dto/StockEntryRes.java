package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import vn.iostar.NT_cinema.entity.Food;
import vn.iostar.NT_cinema.entity.User;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StockEntryRes {
    private String stockEntryId;
    private Food food;
    private String managerId;
    private int quantity;
    private int purchasePrice;
    private Date entryDate;
    private String supplier;
    private int totalPrice;
}
