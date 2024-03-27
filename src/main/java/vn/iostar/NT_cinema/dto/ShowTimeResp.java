package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.iostar.NT_cinema.constant.ShowStatus;
import vn.iostar.NT_cinema.constant.TimeShow;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.entity.Room;
import vn.iostar.NT_cinema.entity.Schedule;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShowTimeResp {
    private String showTimeId;

    private String roomId;

    private String movieId;

    private Date timeStart;

    private Date timeEnd;

    private boolean isSpecial;

    private ShowStatus status;

    private boolean isDelete;
}
