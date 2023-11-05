package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.ShowTimeReq;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.entity.Room;
import vn.iostar.NT_cinema.entity.ShowTime;
import vn.iostar.NT_cinema.repository.MovieRepository;
import vn.iostar.NT_cinema.repository.RoomRepository;
import vn.iostar.NT_cinema.repository.ShowTimeRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ShowTimeService {
    @Autowired
    ShowTimeRepository showTimeRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private MovieRepository movieRepository;

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
            if (showTimeReq.getTime().before(new Date())){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .success(false)
                        .message("The time must later now")
                        .result(null)
                        .statusCode(HttpStatus.CONFLICT.value())
                        .build());
            }
            ShowTime showTime = new ShowTime();
            showTime.setRoom(optionalRoom.get());
            showTime.setMovie(optionalMovie.get());
            showTime.setTime(showTimeReq.getTime());
            showTime.setSpecial(showTimeReq.isSpecial());

            ShowTime nShowTime = showTimeRepository.save(showTime);

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Add showtime success")
                    .result(nShowTime)
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
            showTimeRepository.deleteById(id);
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

    public ResponseEntity<GenericResponse> updateShowTime(String id, ShowTimeReq showTimeReq) {
        try {
            Optional<ShowTime> optionalShowTime = showTimeRepository.findById(id);
            Optional<Movie> optionalMovie = movieRepository.findById(showTimeReq.getMovieId());
            Optional<Room> optionalRoom = roomRepository.findById(showTimeReq.getRoomId());
            if (optionalRoom.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Room not found")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            if (optionalShowTime.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Showtime not found")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            if (showTimeReq.getTime().before(new Date())){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .success(false)
                        .message("The time must later now")
                        .result(null)
                        .statusCode(HttpStatus.CONFLICT.value())
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
            ShowTime showTimed = optionalShowTime.get();
            showTimed.setRoom(optionalRoom.get());
            showTimed.setMovie(optionalMovie.get());
            showTimed.setTime(showTimeReq.getTime());
            showTimed.setSpecial(showTimeReq.isSpecial());

            ShowTime updateShowTime = showTimeRepository.save(showTimed);

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Update showtime success")
                    .result(updateShowTime)
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
            List<ShowTime> showTimes = showTimeRepository.findAllByMovieOrderByTimeAsc(optionalMovie.get());
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
}
