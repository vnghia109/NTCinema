package vn.iostar.NT_cinema.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.service.CloudinaryService;
import vn.iostar.NT_cinema.service.MovieService;
import vn.iostar.NT_cinema.service.ShowTimeService;

import java.util.List;

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
                                                        @RequestParam(defaultValue = "10") int size) {
        return movieService.allMovies(PageRequest.of(index-1, size));
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

    @GetMapping("/{movieId}/show-times")
    public ResponseEntity<GenericResponse> getShowTimesByMovie(@PathVariable("movieId") String movieId) {
        return showTimeService.findShowTimesByMovie(movieId);
    }
    @GetMapping("/search")
    public ResponseEntity<GenericResponse> searchMovie(@RequestParam(defaultValue = "") String keyWord){
        return movieService.searchMovie(keyWord);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> createEvent(@Valid @ModelAttribute List<MultipartFile> file) {
        try {
            if (file != null && !file.isEmpty()) {
                String url = cloudinaryService.uploadImage(file);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Upload file success")
                                .result(url)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(GenericResponse.builder()
                    .success(false)
                    .message("Upload file fail")
                    .result(null)
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(GenericResponse.builder()
                    .success(false)
                    .message("Internal Server Error")
                    .result(e.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }
}
