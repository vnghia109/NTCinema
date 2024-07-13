package vn.iostar.NT_cinema.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "promotionFixed")
public class PromotionFixed {
    @Id
    private String promotionFixedId;
    private String name;
    private String description;
    private BigDecimal coupleValue;
    private BigDecimal vipValue;
    private BigDecimal normalValue;
    private Integer validDayOfWeek;
    private Integer ageLimit;
    private LocalTime validTimeFrameStart;
    private LocalTime validTimeFrameEnd;
    private LocalDate startDate;
    private LocalDate endDate;
    private String image;
    private boolean isDeleted = false;
    private boolean isValid;
    private LocalDate createAt;
}
