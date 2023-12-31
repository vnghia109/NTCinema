package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.TimeShow;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.ShowTimeReq;
import vn.iostar.NT_cinema.dto.ShowTimeResp;
import vn.iostar.NT_cinema.entity.Manager;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.entity.Room;
import vn.iostar.NT_cinema.entity.ShowTime;
import vn.iostar.NT_cinema.repository.ManagerRepository;
import vn.iostar.NT_cinema.repository.MovieRepository;
import vn.iostar.NT_cinema.repository.RoomRepository;
import vn.iostar.NT_cinema.repository.ShowTimeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class ShowTimeService {
    @Autowired
    ShowTimeRepository showTimeRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    ManagerRepository managerRepository;

    public ResponseEntity<GenericResponse> addShowTime(ShowTimeReq showTimeReq) {
        try {
            Optional<Room> optionalRoom = roomRepository.findById(showTimeReq.getRoomId());
            Optional<Movie> optionalMovie = movieRepository.findById(showTimeReq.getMovieId());
            if (optionalRoom.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Room not found")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            if (optionalMovie.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Movie not found")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            Optional<ShowTime> showTimeFind = showTimeRepository.findByMovieAndStatusIsTrueAndRoom(optionalMovie.get(), optionalRoom.get());
            if (showTimeFind.isPresent()){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .success(false)
                        .message("Showtime already exists. You can update the show schedule.")
                        .result(null)
                        .statusCode(HttpStatus.CONFLICT.value())
                        .build());
            }
            ShowTime showTime = new ShowTime();
            showTime.setRoom(optionalRoom.get());
            showTime.setMovie(optionalMovie.get());
            showTime.setTimeStart(showTimeReq.getTimeStart());
            showTime.setTimeEnd(showTimeReq.getTimeEnd());
            showTime.setListTimeShow(showTimeReq.getListTimeShow());
            showTime.setStatus(true);
            showTime.setSpecial(showTimeReq.isSpecial());

            ShowTime nShowTime = showTimeRepository.save(showTime);

            ShowTimeResp showTimeResp = new ShowTimeResp(
                    nShowTime.getShowTimeId(),
                    nShowTime.getRoom().getRoomId(),
                    nShowTime.getMovie().getMovieId(),
                    nShowTime.getTimeStart(),
                    nShowTime.getTimeEnd(),
                    nShowTime.isSpecial(),
                    nShowTime.isStatus(),
                    nShowTime.getListTimeShow());

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Add showtime success")
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
                        .message("Showtime not found")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            optionalShowTime.get().setStatus(!optionalShowTime.get().isStatus());
            ShowTime showTime = showTimeRepository.save(optionalShowTime.get());
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Update status Showtime success")
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

    public ResponseEntity<GenericResponse> updateShowTime(String id, ShowTimeReq showTimeReq) {
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
            Optional<Room> optionalRoom = roomRepository.findById(showTimeReq.getRoomId());
            if (optionalRoom.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Room not found")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            ShowTime showTimed = optionalShowTime.get();
            showTimed.setRoom(optionalRoom.get());
            showTimed.setMovie(optionalMovie.get());
            showTimed.setTimeStart(showTimeReq.getTimeStart());
            showTimed.setTimeEnd(showTimeReq.getTimeEnd());
            showTimed.setListTimeShow(showTimeReq.getListTimeShow());
            showTimed.setSpecial(showTimeReq.isSpecial());

            ShowTime updateShowTime = showTimeRepository.save(showTimed);

            ShowTimeResp showTimeResp = new ShowTimeResp(
                    updateShowTime.getShowTimeId(),
                    updateShowTime.getRoom().getRoomId(),
                    updateShowTime.getMovie().getMovieId(),
                    updateShowTime.getTimeStart(),
                    updateShowTime.getTimeEnd(),
                    updateShowTime.isSpecial(),
                    updateShowTime.isStatus(),
                    updateShowTime.getListTimeShow());

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Update showtime success")
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
            List<ShowTime> showTimes = showTimeRepository.findByMovieAndStatusIsTrue(optionalMovie.get());
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Get show time success")
                    .result(showTimes)
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

    public ResponseEntity<GenericResponse> getShowTimes(Pageable pageable) {
        try {
            Page<ShowTime> showTimes = showTimeRepository.findAllByStatusIsTrue(pageable);

            Map<String, Object> map = new HashMap<>();
            map.put("content", showTimes.getContent());
            map.put("pageNumber", showTimes.getPageable().getPageNumber() + 1);
            map.put("pageSize", showTimes.getSize());
            map.put("totalPages", showTimes.getTotalPages());
            map.put("totalElements", showTimes.getTotalElements());
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

    public ResponseEntity<GenericResponse> adminGetShowTimes(Pageable pageable) {
        try {
            Page<ShowTime> showTimes = showTimeRepository.findAll(pageable);

            Map<String, Object> map = new HashMap<>();
            map.put("content", showTimes.getContent());
            map.put("pageNumber", showTimes.getPageable().getPageNumber() + 1);
            map.put("pageSize", showTimes.getSize());
            map.put("totalPages", showTimes.getTotalPages());
            map.put("totalElements", showTimes.getTotalElements());
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

    public ResponseEntity<GenericResponse> getShowTimesOfManager(String id, Pageable pageable) {
        try {
            Optional<Manager> manager = managerRepository.findById(id);
            if (manager.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Manager not have cinema")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            List<Room> rooms = roomRepository.findAllByCinema_CinemaId(manager.get().getCinema().getCinemaId());
            Page<ShowTime> showTimes = showTimeRepository.findAllByRoomIn(rooms, pageable);

            Map<String, Object> map = new HashMap<>();
            map.put("content", showTimes.getContent());
            map.put("pageNumber", showTimes.getPageable().getPageNumber() + 1);
            map.put("pageSize", showTimes.getSize());
            map.put("totalPages", showTimes.getTotalPages());
            map.put("totalElements", showTimes.getTotalElements());
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

    public ResponseEntity<GenericResponse> getShowtime(String id) {
        try {
            Optional<ShowTime> showTime = showTimeRepository.findById(id);
            return showTime.map(time -> ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Get show time success")
                    .result(time)
                    .statusCode(HttpStatus.OK.value())
                    .build())).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                    .success(false)
                    .message("Show time not found")
                    .result(null)
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .build()));
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
            List<ShowTime> showTimes = showTimeRepository.findAllByRoom_RoomIdAndStatusIsTrue(roomId);
            List<Date> dates = new ArrayList<>();
            for (ShowTime item : showTimes) {
                List<Date> dates1 = getListOfDateTimes(item.getListTimeShow());
                dates.addAll(dates1);
            }
            Collections.sort(dates);

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Get list time show of room success")
                    .result(dates)
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

    public List<Date> getListOfDateTimes(List<TimeShow> listTimeShow) {
        List<Date> listOfDateTimes = new ArrayList<>();

        for (TimeShow timeShow : listTimeShow) {
            LocalDate date = timeShow.getDate();
            List<String> times = timeShow.getTime();

            for (String time : times) {
                LocalDateTime localDateTime = LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(),
                        Integer.parseInt(time.split(":")[0]), Integer.parseInt(time.split(":")[1]));

                Date dateTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                listOfDateTimes.add(dateTime);
            }
        }
        return listOfDateTimes;
    }
}
