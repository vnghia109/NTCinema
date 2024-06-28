package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.AddScheduleReq;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.entity.Schedule;
import vn.iostar.NT_cinema.entity.ShowTime;
import vn.iostar.NT_cinema.exception.NotFoundException;
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
            if (showTime.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Lịch chiếu không tồn tại.")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            Optional<Movie> optionalMovie = movieRepository.findById(showTime.get().getMovie().getMovieId());
            if (optionalMovie.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Phim không tồn tại.")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            LocalTime endTime = scheduleReq.getStartTime().plusMinutes(Integer.parseInt(optionalMovie.get().getDuration()));

            Schedule schedule1 = new Schedule(scheduleReq.getShowTimeId(), scheduleReq.getDate(), scheduleReq.getStartTime(), endTime, showTime.get().getRoom().getRoomId());
            scheduleRepository.save(schedule1);

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Thêm giờ chiếu thành công!")
                    .result(schedule1)
                    .statusCode(HttpStatus.OK.value())
                    .build());

        } catch(Exception e){
            throw new RuntimeException(e.getMessage());
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
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> checkSchedule(String showtimeId, LocalDate date, LocalTime startTime) {
        try {
            Optional<ShowTime> showTime = showTimeRepository.findById(showtimeId);
            if (showTime.isEmpty())
                throw new NotFoundException("Lịch chiếu không tồn tại.");
            List<Schedule> schedules = scheduleRepository.findAllByRoomId(showTime.get().getRoom().getRoomId());
            Optional<Movie> optionalMovie = movieRepository.findById(showTime.get().getMovie().getMovieId());
            if (optionalMovie.isEmpty())
                throw new NotFoundException("Phim không tồn tại.");
            LocalDateTime startNew = LocalDateTime.of(date, startTime);
            LocalDateTime endNew = startNew.plusMinutes(Integer.parseInt(optionalMovie.get().getDuration()));

            if (date.isBefore(showTime.get().getTimeStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()) || date.isAfter(showTime.get().getTimeEnd().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .success(false)
                        .message("Giờ chiếu bắt đầu lúc "+startTime+" ngày "+date+" nằm ngoài thời gian chiếu cho phép.")
                        .result(null)
                        .statusCode(HttpStatus.CONFLICT.value())
                        .build());
            }
            if (startNew.isBefore(LocalDateTime.now())){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .success(false)
                        .message("Thời gian bắt đầu chiếu phải sau thời điểm hiện tại.")
                        .result(null)
                        .statusCode(HttpStatus.CONFLICT.value())
                        .build());
            }
            for (Schedule schedule : schedules) {
                LocalDateTime startOld = LocalDateTime.of(schedule.getDate(), schedule.getStartTime());
                LocalDateTime endOld = LocalDateTime.of(schedule.getStartTime().isAfter(schedule.getEndTime()) ? schedule.getDate().plusDays(1) : schedule.getDate(), schedule.getEndTime());
                if (!(endNew.isBefore(startOld) || startNew.isAfter(endOld))) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                            .success(false)
                            .message("Lịch chiếu bắt đầu lúc "+startTime+" ngày "+date+" bị trùng với lịch chiếu từ "+ schedule.getStartTime()+" đến "+schedule.getEndTime()+" (Lưu ý: các lịch chiếu cách nhau 15 phút).")
                            .result(null)
                            .statusCode(HttpStatus.CONFLICT.value())
                            .build());
                }
                if (!(endNew.plusMinutes(15).isBefore(startOld) || startNew.isAfter(endOld.plusMinutes(15)))) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                            .success(false)
                            .message("Lịch chiếu bắt đầu lúc "+startTime+" ngày "+date+" và lịch chiếu từ "+ schedule.getStartTime()+" đến "+schedule.getEndTime()+" phải cách nhau 15 phút.")
                            .result(null)
                            .statusCode(HttpStatus.CONFLICT.value())
                            .build());
                }
            }
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Giờ chiếu không bị trùng lịch!")
                    .result(null)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> checkScheduleBeforeAddShowtime(String roomId, String movieId, LocalDate date, LocalTime startTime) {
        try {
            List<Schedule> schedules = scheduleRepository.findAllByRoomId(roomId);
            Optional<Movie> optionalMovie = movieRepository.findById(movieId);
            if (optionalMovie.isEmpty())
                throw new NotFoundException("Phim không tồn tại.");
            LocalDateTime startNew = LocalDateTime.of(date, startTime);
            LocalDateTime endNew = startNew.plusMinutes(Integer.parseInt(optionalMovie.get().getDuration()));

            if (startNew.isBefore(LocalDateTime.now())){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .success(false)
                        .message("Thời gian bắt đầu chiếu phải sau thời điểm hiện tại.")
                        .result(null)
                        .statusCode(HttpStatus.CONFLICT.value())
                        .build());
            }
            for (Schedule schedule : schedules) {
                LocalDateTime startOld = LocalDateTime.of(schedule.getDate(), schedule.getStartTime());
                LocalDateTime endOld = LocalDateTime.of(schedule.getStartTime().isAfter(schedule.getEndTime()) ? schedule.getDate().plusDays(1) : schedule.getDate(), schedule.getEndTime());
                if (!(endNew.isBefore(startOld) || startNew.isAfter(endOld))) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                            .success(false)
                            .message("Lịch chiếu bắt đầu lúc "+startTime+" ngày "+date+" bị trùng với lịch chiếu từ "+ schedule.getStartTime()+" đến "+schedule.getEndTime()+" (Lưu ý: các lịch chiếu cách nhau 15 phút).")
                            .result(null)
                            .statusCode(HttpStatus.CONFLICT.value())
                            .build());
                }
                if (!(endNew.plusMinutes(15).isBefore(startOld) || startNew.isAfter(endOld.plusMinutes(15)))) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                            .success(false)
                            .message("Lịch chiếu bắt đầu lúc "+startTime+" ngày "+date+" và lịch chiếu từ "+ schedule.getStartTime()+" đến "+schedule.getEndTime()+" phải cách nhau 15 phút.")
                            .result(null)
                            .statusCode(HttpStatus.CONFLICT.value())
                            .build());
                }
            }
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Giờ chiếu không bị trùng lịch!")
                    .result(null)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
