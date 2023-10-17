package vn.iostar.NT_cinema.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaReq {
    private String location;

    private String cinemaName;

    private String desc;
}
