package vn.iostar.NT_cinema.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.ShowStatus;
import vn.iostar.NT_cinema.constant.TimeShow;
import vn.iostar.NT_cinema.dto.*;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShowTimeService {
    @Autowired
    ShowTimeRepository showTimeRepository;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    MovieRepository movieRepository;
    @Autowired
    ManagerRepository managerRepository;
    @Autowired
    ScheduleRepository scheduleRepository;

    public ShowStatus getShowStatus(ShowTime showTime) {
        LocalDate now = LocalDate.now();
        LocalDate start = showTime.getTimeStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = showTime.getTimeEnd().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if ((start.isBefore(now)|| start.isEqual(now)) && (end.isAfter(now)|| end.isEqual(now))) {
            return ShowStatus.SHOWING;
        } else if (start.isAfter(now)) {
            return ShowStatus.COMING_SOON;
        } else {
            return ShowStatus.ENDED;
        }
    }

    @PostConstruct
    @Scheduled(cron = "0 0 0 * * *")
    public void updateShowTimeStatus() {
        Date now = new Date();
        // Lấy tất cả các ShowTime có startDate và endDate trong khoảng thời gian hiện tại
        List<ShowTime> showTimes = showTimeRepository.findAll();

        for (ShowTime showTime : showTimes) {
            // Thay đổi trang thái
            showTime.setStatus(getShowStatus(showTime));
            // Lưu thay đổi vào database
            showTimeRepository.save(showTime);
        }
    }

    public ResponseEntity<GenericResponse> addShowTime(ShowTimeReq showTimeReq) {
        try {
            Optional<Room> optionalRoom = roomRepository.findById(showTimeReq.getRoomId());
            Optional<Movie> optionalMovie = movieRepository.findById(showTimeReq.getMovieId());
            if (optionalRoom.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Phòng không tồn tại.")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            if (optionalMovie.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Phim không tồn tại.")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            if (showTimeReq.isSpecial()){
                Optional<ShowTime> showTimeFind = showTimeRepository.findByMovieAndRoomAndIsDeleteIsFalseAndIsSpecialIsTrue(optionalMovie.get(), optionalRoom.get());
                if (showTimeFind.isPresent()){
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                            .success(false)
                            .message("Lịch chiếu đặc biệt cho phim "+optionalMovie.get().getTitle()+" ở phòng "+optionalRoom.get().getRoomName()+" đã sẵn sàng. Bạn có thể chỉnh sửa lại lịch chiếu.")
                            .result(null)
                            .statusCode(HttpStatus.CONFLICT.value())
                            .build());
                }
            }else {
                Optional<ShowTime> showTimeFind1 = showTimeRepository.findByMovieAndRoomAndIsDeleteIsFalseAndIsSpecialIsFalse(optionalMovie.get(), optionalRoom.get());
                if (showTimeFind1.isPresent()){
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                            .success(false)
                            .message("Lịch chiếu cho phim "+optionalMovie.get().getTitle()+" ở phòng "+optionalRoom.get().getRoomName()+" đã sẵn sàng. Bạn có thể chỉnh sửa lại lịch chiếu.")
                            .result(null)
                            .statusCode(HttpStatus.CONFLICT.value())
                            .build());
                }
            }
            ShowTime showTime = new ShowTime();
            showTime.setRoom(optionalRoom.get());
            showTime.setMovie(optionalMovie.get());
            showTime.setTimeStart(showTimeReq.getTimeStart());
            showTime.setTimeEnd(showTimeReq.getTimeEnd());
            showTime.setStatus(getShowStatus(showTime));
            showTime.setSpecial(showTimeReq.isSpecial());
            showTime.setCreatedAt(new Date());
            showTime.setUpdatedAt(new Date());

            List<Schedule> schedules = scheduleRepository.findAllByRoomId(showTime.getRoom().getRoomId());

            for (TimeShow item : showTimeReq.getSchedules()) {
                LocalTime endTime = item.getStartTime().plusMinutes(Integer.parseInt(optionalMovie.get().getDuration()));
                if (item.getDate().isBefore(showTime.getTimeStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()) || item.getDate().isAfter(showTime.getTimeEnd().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                            .success(false)
                            .message("Giờ chiếu bắt đầu lúc "+item.getStartTime()+" ngày "+item.getDate()+" nằm ngoài thời gian chiếu cho phép.")
                            .result(null)
                            .statusCode(HttpStatus.CONFLICT.value())
                            .build());
                }
                if (item.getStartTime().isBefore(LocalTime.now())){
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                            .success(false)
                            .message("Thời gian bắt đầu chiếu phải sau thời điểm hiện tại.")
                            .result(null)
                            .statusCode(HttpStatus.CONFLICT.value())
                            .build());
                }
                for (Schedule schedule : schedules) {
                    if (!(item.getStartTime().isAfter(schedule.getEndTime().plusMinutes(15)) || endTime.plusMinutes(15).isBefore(schedule.getStartTime())) && item.getDate().equals(schedule.getDate())) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                                .success(false)
                                .message("Lịch chiếu bắt đầu lúc "+item.getStartTime()+" ngày "+item.getDate()+" bị trùng với lịch chiếu từ "+ schedule.getStartTime()+" đến "+schedule.getEndTime())
                                .result(schedule.getScheduleId())
                                .statusCode(HttpStatus.CONFLICT.value())
                                .build());
                    }
                }
            }

            ShowTime nShowTime = showTimeRepository.save(showTime);

            for (TimeShow item : showTimeReq.getSchedules()) {
                LocalTime endTime = item.getStartTime().plusMinutes(Integer.parseInt(optionalMovie.get().getDuration()));
                Schedule schedule1 = new Schedule(nShowTime.getShowTimeId(), item.getDate(), item.getStartTime(), endTime, nShowTime.getRoom().getRoomId());
                scheduleRepository.save(schedule1);
                schedules.add(schedule1);
            }

            ShowTimeResp showTimeResp = new ShowTimeResp(
                    nShowTime.getShowTimeId(),
                    nShowTime.getRoom().getRoomId(),
                    nShowTime.getMovie().getMovieId(),
                    nShowTime.getTimeStart(),
                    nShowTime.getTimeEnd(),
                    nShowTime.isSpecial(),
                    nShowTime.getStatus(),
                    nShowTime.isDelete());

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Thêm lịch chiếu thành công!")
                    .result(showTimeResp)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> deleteShowTime(String id) {
        try {
            Optional<ShowTime> optionalShowTime = showTimeRepository.findById(id);
            if (optionalShowTime.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Showtime not found")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            showTimeRepository.delete(optionalShowTime.get());
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Delete Showtime success")
                    .result(null)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> updateIsDeleteShowTime(String id) {
        try {
            Optional<ShowTime> optionalShowTime = showTimeRepository.findById(id);
            if (optionalShowTime.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy lịch chiếu")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            if (optionalShowTime.get().getStatus().equals(ShowStatus.SHOWING)){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .success(false)
                        .message("Không thể ngừng lịch đang chiếu.")
                        .result(null)
                        .statusCode(HttpStatus.CONFLICT.value())
                        .build());
            }
            optionalShowTime.get().setDelete(!optionalShowTime.get().isDelete());
            optionalShowTime.get().setUpdatedAt(new Date());
            ShowTime showTime = showTimeRepository.save(optionalShowTime.get());
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Cập nhật lịch chiếu thành công!")
                    .result(showTime)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> updateShowTime(String id, UpdateShowTimeReq showTimeReq) {
        try {
            Optional<ShowTime> optionalShowTime = showTimeRepository.findById(id);
            if (optionalShowTime.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Showtime not found")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            Optional<Movie> optionalMovie = movieRepository.findById(showTimeReq.getMovieId());
            if (optionalMovie.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Movie not found")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            ShowTime showTimed = optionalShowTime.get();
            showTimed.setMovie(optionalMovie.get());
            showTimed.setTimeStart(showTimeReq.getTimeStart());
            showTimed.setTimeEnd(showTimeReq.getTimeEnd());
            showTimed.setSpecial(showTimeReq.isSpecial());
            showTimed.setUpdatedAt(new Date());

            showTimed.setStatus(getShowStatus(showTimed));
            ShowTime updateShowTime = showTimeRepository.save(showTimed);

            ShowTimeResp showTimeResp = new ShowTimeResp(
                    updateShowTime.getShowTimeId(),
                    updateShowTime.getRoom().getRoomId(),
                    updateShowTime.getMovie().getMovieId(),
                    updateShowTime.getTimeStart(),
                    updateShowTime.getTimeEnd(),
                    updateShowTime.isSpecial(),
                    updateShowTime.getStatus(),
                    updateShowTime.isDelete());

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Cập nhật thông tin lịch chiếu thành công!")
                    .result(showTimeResp)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> findShowTimesByMovie(String movieId) {
        try {
            Optional<Movie> optionalMovie = movieRepository.findById(movieId);
            if (optionalMovie.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Movie not found")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            List<ShowScheduleResp> responses = new ArrayList<>();
            List<ShowTime> showTimes = showTimeRepository.findByMovieAndIsDeleteIsFalse(optionalMovie.get());
            for (ShowTime showTime : showTimes) {
                List<Schedule> schedules = scheduleRepository.findAllByShowTimeId(showTime.getShowTimeId())
                        .stream().sorted(Comparator.comparing(Schedule::getDate).thenComparing(Schedule::getStartTime)).collect(Collectors.toList());
                ShowScheduleResp response = new ShowScheduleResp(
                        showTime.getShowTimeId(),
                        showTime.getRoom(),
                        showTime.getMovie(),
                        showTime.getTimeStart(),
                        showTime.getTimeEnd(),
                        showTime.isSpecial(),
                        showTime.getStatus(),
                        showTime.isDelete(),
                        schedules);
                responses.add(response);
            }
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Get show time success")
                    .result(responses)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> getShowTimes(LocalDate date, Pageable pageable) {
        try {
            List<ShowTime> showTimes = showTimeRepository.findAllByIsDeleteIsFalse();
            List<ShowScheduleResp> responses = createShowScheduleResponses(showTimes, date);

            Map<String, Object> map = createResponseMap(responses, pageable);
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Get all show time success")
                    .result(map)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> adminGetShowTimes(LocalDate date, Pageable pageable) {
        try {
            List<ShowTime> showTimes = showTimeRepository.findAllByOrderByShowTimeIdDesc();
            List<ShowScheduleResp> responses = createShowScheduleResponses(showTimes, date);
            Map<String, Object> map = createResponseMap(responses, pageable);
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Get all show time success")
                    .result(map)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> getShowTimesOfManager(LocalDate date, String id, Pageable pageable) {
        try {
            Optional<Manager> manager = managerRepository.findById(id);
            if (manager.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Quản lý chưa được thêm rạp phim.")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            List<Room> rooms = roomRepository.findAllByCinema_CinemaId(manager.get().getCinema().getCinemaId());
            List<ShowTime> showTimes = showTimeRepository.findAllByRoomInOrderByShowTimeIdDesc(rooms);
            List<ShowScheduleResp> responses = createShowScheduleResponses(showTimes, date);
            Map<String, Object> map = createResponseMap(responses, pageable);
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Lấy lịch chiếu thành công!")
                    .result(map)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> getShowtime(String id) {
        try {
            Optional<ShowTime> optionalShowTime = showTimeRepository.findById(id);
            if (optionalShowTime.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Lịch chiếu không tồn tại.")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            ShowTime showTime = optionalShowTime.get();
            List<Schedule> schedules = scheduleRepository.findAllByShowTimeId(showTime.getShowTimeId())
                    .stream().sorted(Comparator.comparing(Schedule::getDate).thenComparing(Schedule::getStartTime)).collect(Collectors.toList());
            ShowScheduleResp response = new ShowScheduleResp(
                    showTime.getShowTimeId(),
                    showTime.getRoom(),
                    showTime.getMovie(),
                    showTime.getTimeStart(),
                    showTime.getTimeEnd(),
                    showTime.isSpecial(),
                    showTime.getStatus(),
                    showTime.isDelete(),
                    schedules);
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Lấy chi tiết lịch chiếu thành công!")
                    .result(response)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> getTimeShowOfRoom(String roomId) {
        try {
            List<Schedule> schedules = scheduleRepository.findAllByRoomId(roomId)
                    .stream().sorted(Comparator.comparing(Schedule::getDate).thenComparing(Schedule::getStartTime)).toList();

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Lấy danh sách lịch chiếu theo phòng thành công!")
                    .result(schedules)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> findShowtimesByCinema(String id, LocalDate date, Pageable pageable) {
        try {
            List<Room> rooms = roomRepository.findAllByCinema_CinemaId(id);
            List<ShowTime> showTimes = showTimeRepository.findAllByRoomInOrderByShowTimeIdDesc(rooms);
            List<ShowScheduleResp> responses = createShowScheduleResponses(showTimes, date);
            Map<String, Object> map = createResponseMap(responses, pageable);
            return ResponseEntity.ok()
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy danh sách lịch chiếu thành công!")
                            .result(map)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ.")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> findShowtimesByRoom(String roomId, LocalDate date, Pageable pageable) {
        try {
            List<ShowTime> showTimes = showTimeRepository.findAllByRoom_RoomIdOrderByShowTimeIdDesc(roomId);

            List<ShowScheduleResp> responses = createShowScheduleResponses(showTimes, date);

            Map<String, Object> map = createResponseMap(responses, pageable);
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Lấy danh sách lịch chiếu thành công!")
                    .result(map)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    private List<ShowScheduleResp> createShowScheduleResponses(List<ShowTime> showTimes, LocalDate date) {
        if (date != null) {
            showTimes = showTimes.stream()
                    .filter(showTime -> isTimeInRange(showTime.getTimeStart(), showTime.getTimeEnd(), date))
                    .collect(Collectors.toList());
        }
        return showTimes.stream()
                .map(showTime -> createShowScheduleResponse(showTime, date))
                .toList();
    }
    private ShowScheduleResp createShowScheduleResponse(ShowTime showTime, LocalDate date) {
        List<Schedule> schedules = scheduleRepository.findAllByShowTimeId(showTime.getShowTimeId());
        if (date != null) {
            schedules = schedules.stream()
                    .filter(schedule -> schedule.getDate().equals(date))
                    .sorted(Comparator.comparing(Schedule::getDate).thenComparing(Schedule::getStartTime))
                    .toList();
        }else {
            schedules = schedules.stream()
                    .sorted(Comparator.comparing(Schedule::getDate).thenComparing(Schedule::getStartTime))
                    .toList();
        }
        return new ShowScheduleResp(
                showTime.getShowTimeId(),
                showTime.getRoom(),
                showTime.getMovie(),
                showTime.getTimeStart(),
                showTime.getTimeEnd(),
                showTime.isSpecial(),
                showTime.getStatus(),
                showTime.isDelete(),
                schedules);
    }
    private Map<String, Object> createResponseMap(List<ShowScheduleResp> responses, Pageable pageable) {
        Map<String, Object> map = new HashMap<>();
        map.put("content", responses.stream().limit(pageable.getPageSize()).collect(Collectors.toList()));
        map.put("pageNumber", pageable.getPageNumber() + 1);
        map.put("pageSize", pageable.getPageSize());
        map.put("totalPages", responses.size()%pageable.getPageSize() == 0 ? (int) responses.size() / pageable.getPageSize() : (int) responses.size() / pageable.getPageSize() + 1);
        map.put("totalElements", responses.size());
        return map;
    }

    private boolean isTimeInRange(Date startTime, Date endTime, LocalDate date) {
        LocalDate start = startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return start.isBefore(date) && end.isAfter(date);
    }
}
