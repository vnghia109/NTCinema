package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.UpdateScheduleReq;
import vn.iostar.NT_cinema.entity.Schedule;
import vn.iostar.NT_cinema.entity.ShowTime;
import vn.iostar.NT_cinema.repository.ScheduleRepository;
import vn.iostar.NT_cinema.repository.ShowTimeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public class ScheduleService {
    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    ShowTimeRepository showTimeRepository;

    public ResponseEntity<GenericResponse> updateSchedule(String id, UpdateScheduleReq scheduleReq) {
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
            Schedule schedule = optionalSchedule.get();
            ShowTime nShowTime = showTimeRepository.findById(schedule.getShowTimeId()).get();
            if (schedule.getDate().isBefore(nShowTime.getTimeStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .success(false)
                        .message("Lịch chiếu không được trước thời gian bắt đầu.")
                        .result(null)
                        .statusCode(HttpStatus.CONFLICT.value())
                        .build());
            }
            schedule.setDate(scheduleReq.getDate());
            schedule.setStartTime(scheduleReq.getStartTime());
            schedule.setEndTime(scheduleReq.getEndTime());
            schedule.setUpdatedAt(new Date());

            scheduleRepository.save(schedule);

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Cập nhật lịch trình thành công!")
                    .result(schedule)
                    .statusCode(HttpStatus.OK.value())
                    .build());

        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Không thể cập nhật lịch trình.")
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
