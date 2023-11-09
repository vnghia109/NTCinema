package vn.iostar.NT_cinema.entity;

import lombok.*;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.iostar.NT_cinema.constant.FoodType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "food")
public class Food {
    @Id
    private String foodId;

    private String name;

    private int price;

    private FoodType foodType;

    private boolean status = true;
}
