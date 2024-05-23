package vn.iostar.NT_cinema.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.iostar.NT_cinema.constant.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "promotionCode")
public class PromotionCode {
    @Id
    private String promotionCodeId;
    @Indexed(unique = true)
    private String promotionCode;
    private Integer maxUsage;
    private Integer useForUserPerDay;
    private String name;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isDeleted = false;
    private boolean isValid = true;
    private LocalDate createAt;
}
