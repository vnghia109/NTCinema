package vn.iostar.NT_cinema.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.*;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.BookingRepository;
import vn.iostar.NT_cinema.repository.MovieRepository;
import vn.iostar.NT_cinema.repository.RoomRepository;
import vn.iostar.NT_cinema.repository.ShowTimeRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    ShowTimeRepository showTimeRepository;
    @Autowired
    CloudinaryService cloudinaryService;
    @Autowired
    RoomRepository roomRepository;

    public ResponseEntity<GenericResponse> allMovies(Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findAllByIsDeleteIsFalse(pageable);
        List<MovieRes> movieRes = moviePage.getContent().stream()
                .map(this::mapCinemaToMovieRes)
                .collect(Collectors.toList());

        Map<String, Object> map = new HashMap<>();
        map.put("content", movieRes);
        map.put("pageNumber", moviePage.getPageable().getPageNumber() + 1);
        map.put("pageSize", moviePage.getSize());
        map.put("totalPages", moviePage.getTotalPages());
        map.put("totalElements", moviePage.getTotalElements());

        return ResponseEntity.status(HttpStatus.OK)
                .body(GenericResponse.builder()
                        .success(true)
                        .message("Get all movie")
                        .result(map)
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .build());
    }

    private MovieRes mapCinemaToMovieRes(Movie movie) {
        MovieRes movieRes = new MovieRes();
        movieRes.setMovieId(movie.getMovieId());
        movieRes.setTitle(movie.getTitle());
        movieRes.setDirector(movie.getDirector());
        movieRes.setGenres(movie.getGenres());
        movieRes.setActor(movie.getActor());
        movieRes.setReleaseDate(movie.getReleaseDate());
        movieRes.setDesc(movie.getDesc());
        movieRes.setPoster(movie.getPoster());
        movieRes.setTrailerLink(movie.getTrailerLink());
        movieRes.setDuration(movie.getDuration());
        movieRes.setRating(String.valueOf(movie.getRating()));

        return movieRes;
    }

    public ResponseEntity<GenericResponse> adminGetAllMovie(Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findAll(pageable);

        Map<String, Object> map = new HashMap<>();
        map.put("content", moviePage.getContent());
        map.put("pageNumber", moviePage.getPageable().getPageNumber() + 1);
        map.put("pageSize", moviePage.getSize());
        map.put("totalPages", moviePage.getTotalPages());
        map.put("totalElements", moviePage.getTotalElements());

        return ResponseEntity.status(HttpStatus.OK)
                .body(GenericResponse.builder()
                        .success(true)
                        .message("Get all movie")
                        .result(map)
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


    public ResponseEntity<GenericResponse> save(MovieReq req) {
        try {
            if (movieRepository.findByTitle(req.getTitle()).isEmpty()) {
                Movie movie = new Movie(req.getTitle(), req.getDirector(), req.getGenres(), req.getActor(), req.getReleaseDate(), req.getDesc(), req.getTrailerLink(), req.getDuration());

                String url = cloudinaryService.uploadImage(req.getPoster());
                movie.setPoster(url);

                Movie movieRes = movieRepository.save(movie);
                BeanUtils.copyProperties(req, movie);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Add movie success")
                                .result(movieRes)
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

    public ResponseEntity<GenericResponse> update(String movieId, MovieReq movieRequest) {
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
                String url = cloudinaryService.uploadImage(movieRequest.getPoster());
                movie.setPoster(url);
                movie.setTrailerLink(movieRequest.getTrailerLink());
                movie.setDuration(movieRequest.getDuration());

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
                movieRepository.delete(optionalMovie.get());
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Delete movie success")
                                .result(null)
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

    public ResponseEntity<GenericResponse> updateIsDelete(String movieId) {
        try {
            Optional<Movie> optionalMovie = movieRepository.findById(movieId);
            if (optionalMovie.isPresent()){
                optionalMovie.get().setDelete(!optionalMovie.get().isDelete());
                movieRepository.save(optionalMovie.get());
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Update status movie success")
                                .result(optionalMovie.get())
                                .statusCode(HttpStatus.OK.value())
                                .build());
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Update failed! movie not found")
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
            List<ShowTime> showTimes = showTimeRepository.findAllByMovieAndIsSpecialIsFalseAndStatusIsTrue(item);
            if (showTimes.isEmpty())
                continue;
            Date max = showTimes.get(0).getTimeEnd();
            Date min = showTimes.get(0).getTimeStart();
            for (ShowTime showTime : showTimes) {
                if (showTime.getTimeStart().before(min)){
                    min = showTime.getTimeStart();
                }
                if (showTime.getTimeEnd().after(max)){
                    max = showTime.getTimeEnd();
                }
            }
            if (min.before(now) && max.after(now)){
                movies.add(item);
            }
        }
        return movies;
    }

    public ResponseEntity<GenericResponse> findNowPlayingMovies() {
        try {
            List<Movie> movieList = movieRepository.findAll();
            List<Movie> movies = checkPlayingMovies(movieList);
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
                List<ShowTime> showTimes = showTimeRepository.findAllByMovieAndIsSpecialIsFalseAndStatusIsTrue(item);
                if (showTimes.isEmpty())
                    continue;
                Date min = showTimes.get(0).getTimeStart();
                for (ShowTime showTime : showTimes) {
                    if (showTime.getTimeStart().before(min)){
                        min = showTime.getTimeStart();
                    }
                }
                if (min.after(now)){
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
            for (Movie item : movieList) {
                List<ShowTime> showTime = showTimeRepository.findAllByMovieAndIsSpecialIsTrueAndStatusIsTrue(item);
                if (showTime.isEmpty())
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
            List<Room> rooms = roomRepository.findAllByCinema_CinemaId(id);
            List<ShowTime> showTimes = showTimeRepository.findAllByRoomInAndStatusIsTrue(rooms);
//            for (ShowTime item: showTimes) {
//                Movie currentMovie = item.getMovie();
//                boolean isDuplicate = false;
//                for (Movie existingMovie : movieList) {
//                    if (currentMovie.getMovieId().equals(existingMovie.getMovieId())) {
//                        isDuplicate = true;
//                        break;
//                    }
//                }
//
//                if (!isDuplicate) {
//                    movieList.add(currentMovie);
//                }
//            }
//            List<Movie> movies = checkPlayingMovies(movieList);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Get all` showtime by cinema success")
                            .result(showTimes)
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

    public ResponseEntity<GenericResponse> searchMovie(String keyWord) {
        try {
            List<Movie> foundMovies = movieRepository.searchMoviesByKeyword(keyWord);

            if (foundMovies.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Movie not found")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Search movie success")
                                .result(foundMovies)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            }
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

    public ResponseEntity<?> getUpcomingMovies(String userId) {
        try {
            List<Booking> bookings = bookingRepository.findAllByUserIdAndIsPaymentIsTrue(userId);
            List<HistoryMovieRes> historyMovieRes = new ArrayList<>();
            for (Booking item : bookings) {
                if (item.getSeats().get(0).getTimeShow().after(new Date())){
                    Optional<ShowTime> showTime = showTimeRepository.findById(item.getSeats().get(0).getShowTimeId());
                    if (showTime.isPresent()){
                        HistoryMovieRes upcoming = new HistoryMovieRes();
                        upcoming.setBookingId(item.getBookingId());
                        upcoming.setMovieId(showTime.get().getMovie().getMovieId());
                        upcoming.setMovieName(showTime.get().getMovie().getTitle());
                        upcoming.setCinemaName(showTime.get().getRoom().getCinema().getCinemaName());
                        upcoming.setTimeShow(item.getSeats().get(0).getTimeShow());
                        upcoming.setPrice(item.getTotal());

                        historyMovieRes.add(upcoming);
                    }
                }
            }
            Collections.reverse(historyMovieRes);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Get list movie upcoming success")
                            .result(historyMovieRes)
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

    public ResponseEntity<?> getViewedMovies(String userId) {
        try {
            List<Booking> bookings = bookingRepository.findAllByUserIdAndIsPaymentIsTrue(userId);
            List<HistoryMovieRes> historyMovieRes = new ArrayList<>();
            Date now = new Date();
            for (Booking item : bookings) {
                Optional<ShowTime> showTime = showTimeRepository.findById(item.getSeats().get(0).getShowTimeId());
                if (showTime.isPresent()){
                    long diffInMinutes = (now.getTime() - (item.getSeats().get(0).getTimeShow().getTime() + Integer.parseInt(showTime.get().getMovie().getDuration()))) / (60 * 1000);
                    if (diffInMinutes > 0){
                        HistoryMovieRes upcoming = new HistoryMovieRes();
                        upcoming.setBookingId(item.getBookingId());
                        upcoming.setMovieId(showTime.get().getMovie().getMovieId());
                        upcoming.setMovieName(showTime.get().getMovie().getTitle());
                        upcoming.setCinemaName(showTime.get().getRoom().getCinema().getCinemaName());
                        upcoming.setTimeShow(item.getSeats().get(0).getTimeShow());
                        upcoming.setPrice(item.getTotal());

                        historyMovieRes.add(upcoming);
                    }
                }
            }
            Collections.reverse(historyMovieRes);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Get list movie viewed success")
                            .result(historyMovieRes)
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
