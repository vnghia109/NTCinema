package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.iostar.NT_cinema.constant.FoodType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodByCinema {
    private String name;

    private String image;

    private int quantity;

    private int price;

    private FoodType foodType;

    private boolean status;
}
