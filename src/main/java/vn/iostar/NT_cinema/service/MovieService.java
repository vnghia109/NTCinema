package vn.iostar.NT_cinema.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.ShowStatus;
import vn.iostar.NT_cinema.dto.*;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    @Autowired
    ScheduleRepository scheduleRepository;

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
                        .message("Lấy tất cả phim thành công!")
                        .result(map)
                        .statusCode(HttpStatus.OK.value())
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
                        .message("Lấy tất cả phim thành công!")
                        .result(map)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    public ResponseEntity<GenericResponse> findMovieById(String id) {
        try {
            Optional<Movie> movie = movieRepository.findById(id);
            return movie.map(value -> ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy thông tin phim thành công!")
                            .result(value)
                            .statusCode(HttpStatus.OK.value())
                            .build())).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Phim không tìm thấy.")
                            .result(null)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .build()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ.")
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
                                .message("Thêm phim thành công!")
                                .result(movieRes)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Phim đã tồn tại.")
                                .result(null)
                                .statusCode(HttpStatus.CONFLICT.value())
                                .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ.")
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
                cloudinaryService.deleteImage(movie.getPoster());
                String url = cloudinaryService.uploadImage(movieRequest.getPoster());
                movie.setPoster(url);
                movie.setTrailerLink(movieRequest.getTrailerLink());
                movie.setDuration(movieRequest.getDuration());

                Movie updateMovie = movieRepository.save(movie);

                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Cập nhật phim thành công!")
                                .result(updateMovie)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Phim không tìm thấy.")
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
                                .message("Xóa phim thành công!")
                                .result(null)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Xóa phim thất bai! Không tìm thấy phim.")
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
                                .message("Cập nhật trạng thái phim thành công!")
                                .result(optionalMovie.get())
                                .statusCode(HttpStatus.OK.value())
                                .build());
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Cập nhật trạng thái phim thất bai! Không tìm thấy phim.")
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

    public ResponseEntity<GenericResponse> findNowPlayingMovies() {
        try {
            List<ShowTime> showTimes = showTimeRepository.findAllByStatusAndIsSpecialIsFalseAndIsDeleteIsFalse(ShowStatus.SHOWING);
            Map<String, Movie> uniqueMoviesMap = new HashMap<>();

            for (ShowTime showTime : showTimes) {
                String movieId = showTime.getMovie().getMovieId();

                if (!uniqueMoviesMap.containsKey(movieId)) {
                    uniqueMoviesMap.put(movieId, showTime.getMovie());
                }
            }

            List<Movie> uniqueMovies = new ArrayList<>(uniqueMoviesMap.values());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy phim đang chiếu thành công!")
                            .result(uniqueMovies)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ.")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> findComingSoonMovies() {
        try {
            List<ShowTime> showTimes = showTimeRepository.findAllByStatusAndIsSpecialIsFalseAndIsDeleteIsFalse(ShowStatus.COMING_SOON);
            Map<String, Movie> uniqueMoviesMap = new HashMap<>();

            for (ShowTime showTime : showTimes) {
                String movieId = showTime.getMovie().getMovieId();

                if (!uniqueMoviesMap.containsKey(movieId)) {
                    uniqueMoviesMap.put(movieId, showTime.getMovie());
                }
            }

            List<Movie> uniqueMovies = new ArrayList<>(uniqueMoviesMap.values());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy phim sắp chiếu thành công!")
                            .result(uniqueMovies)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("lỗi máy chủ.")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> findSpecialMovies() {
        try {
            List<ShowTime> showTimes = showTimeRepository.findAllByIsSpecialIsTrueAndIsDeleteIsFalse();
            Map<String, Movie> uniqueMoviesMap = new HashMap<>();

            for (ShowTime showTime : showTimes) {
                String movieId = showTime.getMovie().getMovieId();

                if (!uniqueMoviesMap.containsKey(movieId)) {
                    uniqueMoviesMap.put(movieId, showTime.getMovie());
                }
            }

            List<Movie> uniqueMovies = new ArrayList<>(uniqueMoviesMap.values());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy xuất chiếu đặc biệt thành công!")
                            .result(uniqueMovies)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ.")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> findNowPlayingMoviesByCinema(String id, Pageable pageable) {
        try {
            List<Room> rooms = roomRepository.findAllByCinema_CinemaId(id);
            Page<ShowTime> showTimes = showTimeRepository.findAllByRoomInAndStatusAndIsDeleteIsFalseAndIsSpecialIsFalse(rooms, ShowStatus.SHOWING, pageable);
            List<ShowScheduleResp> responses = new ArrayList<>();
            for (ShowTime showTime : showTimes.getContent()) {
                List<Schedule> schedules = scheduleRepository.findAllByShowTimeId(showTime.getShowTimeId())
                        .stream().sorted(Comparator.comparing(Schedule::getDate).thenComparing(Schedule::getStartTime)).collect(Collectors.toList());
                ShowScheduleResp response = new ShowScheduleResp(
                        showTime.getShowTimeId(),
                        showTime.getRoom(),
                        showTime.getMovie(),
                        showTime.getTimeStart(),
                        showTime.getTimeEnd(),
                        showTime.isSpecial(),
                        showTime.getStatus(),
                        showTime.isDelete(),
                        schedules);
                responses.add(response);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("content", responses);
            map.put("pageNumber", showTimes.getPageable().getPageNumber() + 1);
            map.put("pageSize", showTimes.getSize());
            map.put("totalPages", showTimes.getTotalPages());
            map.put("totalElements", showTimes.getTotalElements());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy tất cả lịch chiếu của rạp thành công!")
                            .result(map)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ.")
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
                                .message("Phim không tìm thấy.")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Tìm kiếm phim thành công!")
                                .result(foundMovies)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ.")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<?> getUpcomingMovies(String userId) {
        try {
            List<Booking> bookings = bookingRepository.findAllByUserIdAndIsPaymentIsTrue(userId);
            List<HistoryMovieRes> historyMovieRes = new ArrayList<>();
            for (Booking item : bookings) {
                Schedule schedule = item.getSeats().get(0).getSchedule();
                LocalDateTime localDateTime = LocalDateTime.of(schedule.getDate(), schedule.getStartTime());
                Date start = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                if (start.after(new Date())){
                    ShowTime showTime = item.getSeats().get(0).getShowTime();
                    HistoryMovieRes upcoming = new HistoryMovieRes();
                    upcoming.setBookingId(item.getBookingId());
                    upcoming.setMovieId(showTime.getMovie().getMovieId());
                    upcoming.setMovieName(showTime.getMovie().getTitle());
                    upcoming.setCinemaName(showTime.getRoom().getCinema().getCinemaName());
                    upcoming.setDate(schedule.getDate());
                    upcoming.setStartTime(schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                    upcoming.setPrice(item.getTotal());
                    upcoming.setTicketStatus(item.getTicketStatus());

                    historyMovieRes.add(upcoming);
                }
            }
            Collections.reverse(historyMovieRes);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy danh sách phim sắp chiếu thành công!")
                            .result(historyMovieRes)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ.")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<?> getViewedMovies(String userId) {
        try {
            List<Booking> bookings = bookingRepository.findAllByUserIdAndIsPaymentIsTrue(userId);
            List<HistoryMovieRes> historyMovieRes = new ArrayList<>();
            for (Booking item : bookings) {
                ShowTime showTime = item.getSeats().get(0).getShowTime();
                Schedule schedule = item.getSeats().get(0).getSchedule();
                LocalDateTime localDateTime = LocalDateTime.of(schedule.getDate(), schedule.getStartTime());
                Date end = Date.from(localDateTime.plusMinutes(Integer.parseInt(showTime.getMovie().getDuration())).atZone(ZoneId.systemDefault()).toInstant());
                if (end.before(new Date())){
                    HistoryMovieRes viewed = new HistoryMovieRes();
                    viewed.setBookingId(item.getBookingId());
                    viewed.setMovieId(showTime.getMovie().getMovieId());
                    viewed.setMovieName(showTime.getMovie().getTitle());
                    viewed.setCinemaName(showTime.getRoom().getCinema().getCinemaName());
                    viewed.setDate(schedule.getDate());
                    viewed.setStartTime(schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                    viewed.setPrice(item.getTotal());
                    viewed.setTicketStatus(item.getTicketStatus());

                    historyMovieRes.add(viewed);
                }
            }
            Collections.reverse(historyMovieRes);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy danh sách phim đã xem thành công!")
                            .result(historyMovieRes)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ.")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

}
