package vn.iostar.NT_cinema.service;

import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.MovieRequest;
import vn.iostar.NT_cinema.entity.Cinema;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.entity.Room;
import vn.iostar.NT_cinema.entity.ShowTime;
import vn.iostar.NT_cinema.repository.CinemaRepository;
import vn.iostar.NT_cinema.repository.MovieRepository;
import vn.iostar.NT_cinema.repository.RoomRepository;
import vn.iostar.NT_cinema.repository.ShowTimeRepository;

import java.util.*;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    ShowTimeRepository showTimeRepository;

    public ResponseEntity<GenericResponse> allMovies() {
        List<Movie> movieList = movieRepository.findAll();
        return ResponseEntity.status(HttpStatus.OK)
                .body(GenericResponse.builder()
                        .success(true)
                        .message("Get all movie")
                        .result(movieList)
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .build());
    }

    public ResponseEntity<GenericResponse> findById(String id) {
        try {
            Optional<Movie> movie = movieRepository.findById(id);
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

        } catch (Exception e) {
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
            if (movieRepository.findByTitle(entity.getTitle()).isEmpty()) {
                movieRepository.save(entity);
                Movie movie = new Movie();
                BeanUtils.copyProperties(entity, movie);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Add movie success")
                                .result(movie)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Movie already exist")
                                .result(null)
                                .statusCode(HttpStatus.CONFLICT.value())
                                .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Internal Server Error")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> update(String movieId, MovieRequest movieRequest) {
        try {
            Optional<Movie> optionalMovie = movieRepository.findById(movieId);
            if (optionalMovie.isPresent()){
                Movie movie = optionalMovie.get();
                movie.setTitle(movieRequest.getTitle());
                movie.setDirector(movieRequest.getDirector());
                movie.setGenres(movieRequest.getGenres());
                movie.setActor(movieRequest.getActor());
                movie.setDesc(movieRequest.getDesc());
                movie.setReleaseDate(movieRequest.getReleaseDate());
                movie.setPoster(movieRequest.getPoster());
                movie.setTrailerLink(movieRequest.getTrailerLink());

                Movie updateMovie = movieRepository.save(movie);

                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Update movie success")
                                .result(updateMovie)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Movie not found")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> delete(String movieId) {
        try {
            Optional<Movie> optionalMovie = movieRepository.findById(movieId);
            if (optionalMovie.isPresent()){
                movieRepository.deleteById(movieId);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Delete movie success")
                                .result(optionalMovie.get())
                                .statusCode(HttpStatus.OK.value())
                                .build());
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Delete failed! movie not found")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public List<Movie> checkPlayingMovies(List<Movie> movieList){
        Date now = new Date();
        List<Movie> movies = new ArrayList<>();
        for (Movie item : movieList) {
            List<ShowTime> list = showTimeRepository.findAllByMovieOrderByTimeAsc(item);
            if (list.isEmpty())
                continue;
            if (list.get(0).getTime().before(now) && list.get(list.size()-1).getTime().after(now)){
                movies.add(item);
            }
        }
        return movies;
    }

    public ResponseEntity<GenericResponse> findNowPlayingMovies() {
        try {
            List<Movie> movieList = movieRepository.findAll();
            List<Movie> movies = checkPlayingMovies(movieList);
//            Date now = new Date();
//            for (Movie item : movieList) {
//                List<ShowTime> list = showTimeRepository.findAllByMovieOrderByTimeAsc(item);
//                if (list.isEmpty())
//                    continue;
//                if (list.get(0).getTime().before(now) && list.get(list.size()-1).getTime().after(now)){
//                    movies.add(item);
//                }
//            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Get now playing movie success")
                            .result(movies)
                            .statusCode(HttpStatus.OK.value())
                            .build());
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

    public ResponseEntity<GenericResponse> findComingSoonMovies() {
        try {
            List<Movie> movieList = movieRepository.findAll();
            List<Movie> movies = new ArrayList<>();
            Date now = new Date();
            for (Movie item : movieList) {
                List<ShowTime> list = showTimeRepository.findAllByMovieOrderByTimeAsc(item);
                if (list.isEmpty())
                    continue;
                if (list.get(0).getTime().after(now)){
                    movies.add(item);
                }
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Get coming soon movie success")
                            .result(movies)
                            .statusCode(HttpStatus.OK.value())
                            .build());
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

    public ResponseEntity<GenericResponse> findSpecialMovies() {
        try {
            List<Movie> movieList = movieRepository.findAll();
            List<Movie> movies = new ArrayList<>();
            Date now = new Date();
            for (Movie item : movieList) {
                List<ShowTime> list = showTimeRepository.findAllByMovieAndIsSpecialIsTrue(item);
                if (list.isEmpty())
                    continue;

                movies.add(item);

            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Get special movie success")
                            .result(movies)
                            .statusCode(HttpStatus.OK.value())
                            .build());
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

    public ResponseEntity<GenericResponse> findNowPlayingMoviesByCinema(String id) {
        try {
            List<Movie> movieList = new ArrayList<>();
            List<ShowTime> showTimes = showTimeRepository.findAllByRoom_Cinema_CinemaId(id);
            for (ShowTime item: showTimes) {
                Movie currentMovie = item.getMovie();
                boolean isDuplicate = false;
//                if (!movieList.contains(item.getMovie())){
//                    movieList.add(item.getMovie());
//                }
                for (Movie existingMovie : movieList) {
                    if (currentMovie.getMovieId().equals(existingMovie.getMovieId())) {
                        isDuplicate = true;
                        break;
                    }
                }

                if (!isDuplicate) {
                    movieList.add(currentMovie);
                }
            }
            List<Movie> movies = checkPlayingMovies(movieList);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Get movie by cinema success")
                            .result(movies)
                            .statusCode(HttpStatus.OK.value())
                            .build());
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
}
