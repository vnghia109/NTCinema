package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import vn.iostar.NT_cinema.constant.FoodType;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FoodReq {
    @NotBlank
    @NotEmpty
    private String name;

    private int price;

    @NotBlank
    @NotEmpty
    private String foodType;

    @NotEmpty
    private List<MultipartFile> image;
}
