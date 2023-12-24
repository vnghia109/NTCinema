package vn.iostar.NT_cinema.dto;

import lombok.*;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TotalRevenueReq {
    private Date startDate;
    private Date endDate;
}
