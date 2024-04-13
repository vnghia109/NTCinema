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
@Document(collection = "foodInventory")
public class FoodInventory {
    @Id
    private String foodInventoryId;
    @DBRef
    private Food food;
    @DBRef
    private Cinema cinema;
    private int quantity;
    private Date updateAt;
}
