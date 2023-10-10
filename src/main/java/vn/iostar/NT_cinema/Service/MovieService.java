package vn.iostar.NT_cinema.Service;

import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.DTO.GenericResponse;
import vn.iostar.NT_cinema.Entity.Movie;
import vn.iostar.NT_cinema.Repository.MovieRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    public List<Movie> allMovies() {
        return movieRepository.findAll();
    }

    public ResponseEntity<GenericResponse> findById(String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            Optional<Movie> movie = movieRepository.findById(objectId);
            return movie.map(value -> ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Get movie success")
                            .result(value)
                            .statusCode(HttpStatus.OK.value())
                            .build())).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Movie not found")
                            .result(null)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .build()));

        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Internal Server Error")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }

    }


    public ResponseEntity<GenericResponse> save(Movie entity) {
        try {
            if (movieRepository.findByTitle(entity.getTitle()).isEmpty()){
                movieRepository.save(entity);
                Movie movie =new Movie();
                BeanUtils.copyProperties(entity, movie);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Add movie success")
                                .result(movie)
                                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .build());
            }else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Movie already exist")
                                .result(null)
                                .statusCode(HttpStatus.CONFLICT.value())
                                .build());
            }
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Internal Server Error")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }
}
