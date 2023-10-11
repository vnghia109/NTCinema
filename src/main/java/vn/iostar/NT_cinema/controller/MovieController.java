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
    public ResponseEntity<List<Movie>> getAllMovies() {
        return new ResponseEntity<List<Movie>>(movieService.allMovies(), HttpStatus.OK);
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<GenericResponse> getMovie(@PathVariable("movieId") String movieId){
        return movieService.findById(movieId);
    }

    @PostMapping("/movie")
    public ResponseEntity<GenericResponse> addMovie(@RequestBody Movie movie){
        return movieService.save(movie);
    }
}
