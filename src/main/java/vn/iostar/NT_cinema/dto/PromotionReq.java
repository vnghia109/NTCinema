package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;
import vn.iostar.NT_cinema.constant.DiscountType;
import vn.iostar.NT_cinema.constant.PromotionType;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PromotionReq {
    private String name;
    private String promotionType;
    private String promotionCode;
    private int maxUsage;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private int validDayOfWeek;
    private int ageLimit;
    private LocalTime validTimeFrameStart;
    private LocalTime validTimeFrameEnd;
    private Date startDate;
    private Date endDate;
    private boolean excludeHolidays;
}
