package vn.iostar.NT_cinema.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "stockEntry")
public class StockEntry {
    @Id
    private String stockEntryId;
    @DBRef
    private Food food;
    @DBRef
    private User manager;
    private int quantity;
    private int purchasePrice;
    private Date entryDate;
    private String supplier;
    private int totalPrice;
}
