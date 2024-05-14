package vn.iostar.NT_cinema.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.iostar.NT_cinema.constant.DiscountType;

import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "promotionCode")
public class PromotionCode {
    @Id
    private String promotionCodeId;
    @UniqueElements(message = "Mã khuyến mãi đã tồn tại.")
    private String promotionCode;
    private Integer maxUsage;
    private Integer useForUserPerDay;
    private String name;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private boolean isDeleted = false;
    private Date createAt;
}
