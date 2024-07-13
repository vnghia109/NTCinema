package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.NotEmpty;
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

    @NotEmpty(message = "Tên không được để trống.")
    private String name;

    @NotEmpty(message = "Mô tả không được để trống.")
    private String description;

    @NotEmpty(message = "Hình thức khuyến mãi không được để trống.")
    private String discountType;

    @NotEmpty(message = "Giá trị khuyến mãi không được để trống.")
    private BigDecimal discountValue;

    @NotEmpty(message = "Giá trị khuyến mãi tối đa không được để trống.")
    private BigDecimal maxDiscountAmount;

    @NotEmpty(message = "Giá trị khuyến mãi tối thiểu không được để trống.")
    private BigDecimal minOrderValue;

    @NotEmpty(message = "Ngày bắt đầu không được để trống.")
    private LocalDate startDate;

    @NotEmpty(message = "Ngày kết thúc không được để trống.")
    private LocalDate endDate;

    private MultipartFile image;
}
