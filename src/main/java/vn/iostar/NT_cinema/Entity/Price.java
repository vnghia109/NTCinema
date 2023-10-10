package vn.iostar.NT_cinema.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.iostar.NT_cinema.Constant.PriceType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "price")
public class Price {
    @Id
    private String priceId;

    private PriceType type;

    private int price;
}
