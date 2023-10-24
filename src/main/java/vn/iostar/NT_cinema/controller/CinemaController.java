package vn.iostar.NT_cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.service.CinemaService;

@RestController
@RequestMapping("/api/v1/cinemas")
public class CinemaController {
    @Autowired
    CinemaService cinemaService;

    @GetMapping
    public ResponseEntity<GenericResponse> getAllMovies() {
        return cinemaService.allMovies();
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<GenericResponse> getMovie(@PathVariable("movieId") String movieId) {
        return cinemaService.findById(movieId);
    }
}
