package vn.iostar.NT_cinema.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TimeShow {

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;
}
