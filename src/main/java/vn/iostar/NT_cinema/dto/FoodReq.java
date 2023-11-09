package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.iostar.NT_cinema.constant.FoodType;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FoodReq {
    @NotBlank
    @NotEmpty
    private String name;

    @NotBlank
    @NotEmpty
    private int price;

    @NotBlank
    @NotEmpty
    private String foodType;
}
