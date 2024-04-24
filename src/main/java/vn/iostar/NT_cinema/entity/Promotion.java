package vn.iostar.NT_cinema.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.iostar.NT_cinema.constant.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "promotion")
public class Promotion {
    @Id
    private String promotionId;
    private String name;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private Integer validDayOfWeek;
    private Integer ageLimit;
    private LocalTime validTimeFrameStart;
    private LocalTime validTimeFrameEnd;
    private Date startDate;
    private Date endDate;
    private boolean excludeHolidays;
    private boolean isDeleted = false;
}
