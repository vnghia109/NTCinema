package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PromotionFixedReq {
    @NotEmpty(message = "Tên không được để trống.")
    private String name;

    @NotEmpty(message = "Mô tả không được để trống.")
    private String description;

    @NotEmpty(message = "Không được để trống giá trị khuyến mãi.")
    private BigDecimal coupleValue;

    @NotEmpty(message = "Không được để trống giá trị khuyến mãi.")
    private BigDecimal vipValue;

    @NotEmpty(message = "Không được để trống giá trị khuyến mãi.")
    private BigDecimal normalValue;

    private Integer validDayOfWeek;
    private Integer ageLimit;
    private LocalTime validTimeFrameStart;
    private LocalTime validTimeFrameEnd;

    private LocalDate startDate;

    private LocalDate endDate;

    private MultipartFile image;
}
