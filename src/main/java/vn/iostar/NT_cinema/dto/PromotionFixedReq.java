package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

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
    private BigDecimal coupleValue;
    private BigDecimal vipValue;
    private BigDecimal normalValue;
    private Integer validDayOfWeek;
    private Integer ageLimit;
    private LocalTime validTimeFrameStart;
    private LocalTime validTimeFrameEnd;
    private LocalDate startDate;
    private LocalDate endDate;
    private MultipartFile image;
}
