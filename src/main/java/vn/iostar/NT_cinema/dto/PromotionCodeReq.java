package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PromotionCodeReq {
    @Size(min = 6, max = 8, message = "Mã phải dài từ 6 - 8 ký tự")
    private String promotionCode;
    private Integer maxUsage;
    private Integer useForUserPerDay;
    private String name;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private MultipartFile image;
}
