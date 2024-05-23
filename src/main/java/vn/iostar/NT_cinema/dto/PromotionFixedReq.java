package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.iostar.NT_cinema.constant.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PromotionFixedReq {
    private String name;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private Integer validDayOfWeek;
    private Integer ageLimit;
    private LocalTime validTimeFrameStart;
    private LocalTime validTimeFrameEnd;
    private LocalDate startDate;
    private LocalDate endDate;
}
