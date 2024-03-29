package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.iostar.NT_cinema.constant.ShowStatus;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.entity.Room;
import vn.iostar.NT_cinema.entity.Schedule;

import java.util.Date;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ShowScheduleResp {
    private String showTimeId;

    private Room room;

    private Movie movie;

    private Date timeStart;

    private Date timeEnd;

    private boolean isSpecial;

    private ShowStatus status;

    private boolean isDelete;

    private List<Schedule> schedules;
}
