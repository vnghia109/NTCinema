package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PromotionCodeReq {
    private String promotionCode;
    private Integer maxUsage;
    private Integer useForUserPerDay;
    private String name;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private LocalDate startDate;
    private LocalDate endDate;
}
