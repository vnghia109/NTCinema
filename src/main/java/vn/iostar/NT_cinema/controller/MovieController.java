package vn.iostar.NT_cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.service.MovieService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {
    @Autowired
    private MovieService movieService;

    @GetMapping
    public ResponseEntity<GenericResponse> getAllMovies() {
        return movieService.allMovies();
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<GenericResponse> getMovie(@PathVariable("movieId") String movieId) {
        return movieService.findById(movieId);
    }

    @GetMapping("/now-playing")
    public ResponseEntity<GenericResponse> getNowPlayingMovies() {
        return movieService.findNowPlayingMovies();
    }

    @GetMapping("/coming-soon")
    public ResponseEntity<GenericResponse> getComingSoonMovies() {
        return movieService.findComingSoonMovies();
    }

    @GetMapping("/special-movie")
    public ResponseEntity<GenericResponse> getSpecialMovies() {
        return movieService.findSpecialMovies();
    }
}
