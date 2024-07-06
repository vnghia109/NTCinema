package vn.iostar.NT_cinema.entity;

import lombok.*;
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
    @NonNull
    private Integer maxUsage;
    @NonNull
    private Integer useForUserPerDay;
    @NonNull
    private String name;
    private String description;
    @NonNull
    private DiscountType discountType;
    @NonNull
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderValue;
    @NonNull
    private LocalDate startDate;
    @NonNull
    private LocalDate endDate;
    private String image;
    private boolean isDeleted = false;
    private boolean isValid = true;
    private LocalDate createAt = LocalDate.now();
}
