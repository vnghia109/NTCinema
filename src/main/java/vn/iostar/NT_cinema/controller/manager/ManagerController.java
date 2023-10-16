package vn.iostar.NT_cinema.controller.manager;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.MovieRequest;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.security.JwtTokenProvider;
import vn.iostar.NT_cinema.service.MovieService;

@RestController
@PreAuthorize("hasRole('MANAGER')")
@RequestMapping("/api/v1/manager")
public class ManagerController {
    @Autowired
    MovieService movieService;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

//    @PostMapping("/movies/movie")
//    public ResponseEntity<GenericResponse> addMovie(@RequestBody Movie movie) {
//        return movieService.save(movie);
//    }

    @PutMapping("/movies/{movieId}")
    public ResponseEntity<GenericResponse> updateMovie(@PathVariable("movieId") String movieId,
                                                       @RequestBody MovieRequest movieRequest,
                                                       BindingResult bindingResult) throws Exception{
//        String token = authHeader.substring(7);
//        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new GenericResponse(
                    false,
                    "Invalid input data!",
                    null,
                    HttpStatus.BAD_REQUEST.value()));
        }
        return movieService.update(movieId, movieRequest);
    }

    @DeleteMapping("/movies/{movieId}")
    public ResponseEntity<GenericResponse> deleteMovie(@PathVariable("movieId") String movieId) throws Exception{
        return movieService.delete(movieId);
    }
}
