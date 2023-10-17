package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.CinemaReq;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.entity.Cinema;
import vn.iostar.NT_cinema.repository.CinemaRepository;

import java.util.Optional;

@Service
public class CinemaService {
    @Autowired
    CinemaRepository cinemaRepository;

    public ResponseEntity<GenericResponse> getAllCinema(){
        return ResponseEntity.ok().body(GenericResponse.builder()
                .success(true)
                .message("Get all cinema success")
                .result(cinemaRepository.findAll())
                .statusCode(200)
                .build());
    }
    public ResponseEntity<GenericResponse> addCinema(CinemaReq cinemaReq) {
        try {
            if (cinemaRepository.findByCinemaName(cinemaReq.getCinemaName()).isPresent()){
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Cinema name already in use")
                                .result(null)
                                .statusCode(HttpStatus.CONTINUE.value())
                                .build());
            }
            Cinema cinema = new Cinema();
            cinema.setCinemaName(cinemaReq.getCinemaName());
            cinema.setLocation(cinemaReq.getLocation());
            cinema.setDesc(cinemaReq.getDesc());

            Cinema nCinema = cinemaRepository.save(cinema);

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Add cinema success")
                    .result(nCinema)
                    .statusCode(200)
                    .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> updateCinema(String cinemaId, CinemaReq cinemaReq) {
        try {
            Optional<Cinema> optionalCinema = cinemaRepository.findById(cinemaId);
            if (optionalCinema.isPresent()){
                Cinema cinema = optionalCinema.get();
                cinema.setCinemaName(cinemaReq.getCinemaName());
                cinema.setLocation(cinemaReq.getLocation());
                cinema.setDesc(cinemaReq.getDesc());

                Cinema updateCinema = cinemaRepository.save(cinema);

                return ResponseEntity.ok().body(GenericResponse.builder()
                        .success(true)
                        .message("Update cinema success")
                        .result(updateCinema)
                        .statusCode(200)
                        .build());
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Update fail")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }
}
