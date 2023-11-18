package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.iostar.NT_cinema.constant.PriceType;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PriceReq {

    private String type;

    private int price;
}
