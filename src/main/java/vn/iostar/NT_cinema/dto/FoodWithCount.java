package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.iostar.NT_cinema.entity.Food;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FoodWithCount {
    private Food food;
    private int count;
}
