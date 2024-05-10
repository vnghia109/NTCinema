package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.TimeShow;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.AddScheduleReq;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.entity.Schedule;
import vn.iostar.NT_cinema.entity.ShowTime;
import vn.iostar.NT_cinema.repository.MovieRepository;
import vn.iostar.NT_cinema.repository.ScheduleRepository;
import vn.iostar.NT_cinema.repository.ShowTimeRepository;

import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleService {
    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    ShowTimeRepository showTimeRepository;
    @Autowired
    MovieRepository movieRepository;

    public ResponseEntity<GenericResponse> addSchedule(AddScheduleReq scheduleReq) {
        try {
            Optional<ShowTime> showTime = showTimeRepository.findById(scheduleReq.getShowTimeId());
            List<Schedule> schedules = scheduleRepository.findAllByRoomId(showTime.get().getRoom().getRoomId());
            Optional<Movie> optionalMovie = movieRepository.findById(showTime.get().getMovie().getMovieId());
            LocalTime endTime = scheduleReq.getStartTime().plusMinutes(Integer.parseInt(optionalMovie.get().getDuration()));

            if (scheduleReq.getDate().isBefore(showTime.get().getTimeStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()) || scheduleReq.getDate().isAfter(showTime.get().getTimeEnd().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .success(false)
                        .message("Giờ chiếu bắt đầu lúc "+scheduleReq.getStartTime()+" ngày "+scheduleReq.getDate()+" nằm ngoài thời gian chiếu cho phép.")
                        .result(null)
                        .statusCode(HttpStatus.CONFLICT.value())
                        .build());
            }
            if (scheduleReq.getStartTime().plusMinutes(Integer.parseInt(optionalMovie.get().getDuration())).isAfter(endTime)){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .success(false)
                        .message("Thời gian chiếu phải dài hơn thời lượng phim")
                        .result(null)
                        .statusCode(HttpStatus.CONFLICT.value())
                        .build());
            }
            for (Schedule schedule : schedules) {
                LocalDateTime startNew = LocalDateTime.of(scheduleReq.getDate(), scheduleReq.getStartTime());
                LocalDateTime endNew = startNew.plusMinutes(Integer.parseInt(optionalMovie.get().getDuration())+15);
                LocalDateTime startOld = LocalDateTime.of(schedule.getDate(), schedule.getStartTime());
                LocalDateTime endOld = startOld.plusMinutes(Integer.parseInt(optionalMovie.get().getDuration())+15);
                if (startNew.isBefore(endOld) && endNew.isAfter(startOld)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                            .success(false)
                            .message("Lịch chiếu bắt đầu lúc "+scheduleReq.getStartTime()+" ngày "+scheduleReq.getDate()+" bị trùng với lịch chiếu từ "+ schedule.getStartTime()+" đến "+schedule.getEndTime())
                            .result(null)
                            .statusCode(HttpStatus.CONFLICT.value())
                            .build());
                }
            }
            Schedule schedule1 = new Schedule(scheduleReq.getShowTimeId(), scheduleReq.getDate(), scheduleReq.getStartTime(), endTime, showTime.get().getRoom().getRoomId());
            scheduleRepository.save(schedule1);

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Thêm giờ chiếu thành công!")
                    .result(schedule1)
                    .statusCode(HttpStatus.OK.value())
                    .build());

        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Không thể thêm giờ chiếu.")
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> deleteSchedule(String id) {
        try {
            Optional<Schedule> optionalSchedule = scheduleRepository.findById(id);
            if (optionalSchedule.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Lịch trình không tồn tại.")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            LocalDate localDate = optionalSchedule.get().getDate();
            LocalTime localTime = optionalSchedule.get().getStartTime();
            ZoneId zoneId = ZoneId.systemDefault();
            ZonedDateTime zonedDateTime = localDate.atTime(localTime).atZone(zoneId);
            Date date = Date.from(zonedDateTime.toInstant());
            if (date.before(new Date())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .success(false)
                        .message("Không thể xóa lịch đã được chiếu.")
                        .result(null)
                        .statusCode(HttpStatus.CONFLICT.value())
                        .build());
            }
            scheduleRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Xóa lịch trình thành công!")
                    .result(null)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Không thể xóa lịch trình.")
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }
}
