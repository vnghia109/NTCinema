package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;
import vn.iostar.NT_cinema.constant.TimeShow;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.entity.Room;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShowTimeReq {
    @NotBlank(message = "Room id không được để trống")
    private String roomId;
    @NotBlank(message = "Movie id không được để trống")
    private String movieId;
    private Date timeStart;
    private Date timeEnd;
    private boolean special;
    private List<TimeShow> schedules;
}
