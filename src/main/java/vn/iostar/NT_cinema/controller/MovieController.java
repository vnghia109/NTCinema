package vn.iostar.NT_cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.service.CloudinaryService;
import vn.iostar.NT_cinema.service.MovieService;
import vn.iostar.NT_cinema.service.ShowTimeService;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {
    @Autowired
    private MovieService movieService;
    @Autowired
    ShowTimeService showTimeService;
    @Autowired
    CloudinaryService cloudinaryService;

    @GetMapping
    public ResponseEntity<GenericResponse> getAllMovies(@RequestParam(defaultValue = "1") int index,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(defaultValue = "") String genresId) {
        return movieService.allMovies(genresId, PageRequest.of(index-1, size));
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<GenericResponse> getMovie(@PathVariable("movieId") String movieId) {
        return movieService.findMovieById(movieId);
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

    @GetMapping("/{movieId}/show-times")
    public ResponseEntity<GenericResponse> getShowTimesByMovie(@PathVariable("movieId") String movieId) {
        return showTimeService.findShowTimesByMovie(movieId);
    }
    @GetMapping("/search")
    public ResponseEntity<GenericResponse> searchMovie(@RequestParam(defaultValue = "") String keyWord){
        return movieService.searchMovie(keyWord);
    }
}
