package vn.iostar.NT_cinema.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TotalRevenueReq {
    private Date startDate;
    private Date endDate;
}
