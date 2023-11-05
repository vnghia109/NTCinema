package vn.iostar.NT_cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.service.CinemaService;
import vn.iostar.NT_cinema.service.MovieService;

@RestController
@RequestMapping("/api/v1/cinemas")
public class CinemaController {
    @Autowired
    CinemaService cinemaService;

    @Autowired
    MovieService movieService;
    @GetMapping
    public ResponseEntity<GenericResponse> getAllCinema() {
        return cinemaService.getAllCinema();
    }

    @GetMapping("/{Id}")
    public ResponseEntity<GenericResponse> getCinema(@PathVariable("Id") String Id) {
        return cinemaService.findById(Id);
    }

    @GetMapping("/{Id}/movies")
    public ResponseEntity<GenericResponse> getMovies(@PathVariable("Id") String Id) {
        return movieService.findNowPlayingMoviesByCinema(Id);
    }
}
