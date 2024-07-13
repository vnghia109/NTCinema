package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Không được để trống giá trị khuyến mãi.")
    private BigDecimal coupleValue;

    @NotNull(message = "Không được để trống giá trị khuyến mãi.")
    private BigDecimal vipValue;

    @NotNull(message = "Không được để trống giá trị khuyến mãi.")
    private BigDecimal normalValue;

    private Integer validDayOfWeek;
    private Integer ageLimit;
    private LocalTime validTimeFrameStart;
    private LocalTime validTimeFrameEnd;

    @NotNull(message = "Ngày bắt đầu không được để trống.")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống.")
    private LocalDate endDate;

    private MultipartFile image;
}
