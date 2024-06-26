package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.CinemaReq;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.CinemaRepository;
import vn.iostar.NT_cinema.repository.ManagerRepository;
import vn.iostar.NT_cinema.repository.RoomRepository;
import vn.iostar.NT_cinema.repository.ShowTimeRepository;

import java.util.*;

@Service
public class CinemaService {
    @Autowired
    CinemaRepository cinemaRepository;

    @Autowired
    ManagerRepository managerRepository;

    @Autowired
    ShowTimeRepository showTimeRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    RoleService roleService;

    public ResponseEntity<GenericResponse> getAllCinema(Pageable pageable){
        try {
            Page<Cinema> cinemas = cinemaRepository.findAllByStatusIsTrue(pageable);

            Map<String, Object> map = new HashMap<>();
            map.put("content", cinemas.getContent());
            map.put("pageNumber", cinemas.getPageable().getPageNumber() + 1);
            map.put("pageSize", cinemas.getSize());
            map.put("totalPages", cinemas.getTotalPages());
            map.put("totalElements", cinemas.getTotalElements());
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Lấy tất cả rạp phim thành công!")
                    .result(map)
                    .statusCode(200)
                    .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> adminGetAllCinema(boolean status, Pageable pageable){
        try {
            Page<Cinema> cinemas = cinemaRepository.findAllByStatusOrderByCinemaIdDesc(status, pageable);

            Map<String, Object> map = new HashMap<>();
            map.put("content", cinemas.getContent());
            map.put("pageNumber", cinemas.getPageable().getPageNumber() + 1);
            map.put("pageSize", cinemas.getSize());
            map.put("totalPages", cinemas.getTotalPages());
            map.put("totalElements", cinemas.getTotalElements());
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Lấy tất cả rạp phim thành công!")
                    .result(map)
                    .statusCode(200)
                    .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> addCinema(CinemaReq cinemaReq) {
        try {
            if (cinemaRepository.findByCinemaName(cinemaReq.getCinemaName()).isPresent()){
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Tên rạp phim đã được sử dụng.")
                                .result(null)
                                .statusCode(HttpStatus.CONTINUE.value())
                                .build());
            }
            Cinema cinema = new Cinema();
            cinema.setCinemaName(cinemaReq.getCinemaName());
            cinema.setLocation(cinemaReq.getLocation());
            cinema.setDesc(cinemaReq.getDesc());
            cinema.setUrlLocation(cinemaReq.getUrlLocation());
            cinema.setStatus(true);

            Cinema nCinema = cinemaRepository.save(cinema);

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Thêm rạp phim thành công.")
                    .result(nCinema)
                    .statusCode(200)
                    .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> updateCinema(String cinemaId, CinemaReq cinemaReq) {
        try {
            Optional<Cinema> optionalCinema = cinemaRepository.findById(cinemaId);
            if (optionalCinema.isPresent()){
                Cinema cinema = optionalCinema.get();
                cinema.setCinemaName(cinemaReq.getCinemaName());
                cinema.setLocation(cinemaReq.getLocation());
                cinema.setDesc(cinemaReq.getDesc());
                cinema.setUrlLocation(cinemaReq.getUrlLocation());

                Cinema updateCinema = cinemaRepository.save(cinema);

                return ResponseEntity.ok().body(GenericResponse.builder()
                        .success(true)
                        .message("Cập nhật rạp phim thành công.")
                        .result(updateCinema)
                        .statusCode(200)
                        .build());
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Cập nhật rạp phim thất bại.")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }


    public ResponseEntity<GenericResponse> findById(String movieId) {
        try {
            Optional<Cinema> cinema = cinemaRepository.findById(movieId);
            return cinema.map(value -> ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy rạp phim thành công!")
                            .result(value)
                            .statusCode(HttpStatus.OK.value())
                            .build())).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Không tìm thấy rạp phim.")
                            .result(null)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .build()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi hệ thống.")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> deleteCinema(String cinemaId) {
        try {
            Optional<Cinema> cinema = cinemaRepository.findById(cinemaId);
            if (cinema.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Không tìm thấy rạp phim.")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            cinemaRepository.delete(cinema.get());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Xóa rạp phim thành công!")
                            .result(null)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> updateIsDeleteCinema(String cinemaId) {
        try {
            Optional<Cinema> cinema = cinemaRepository.findById(cinemaId);
            if (cinema.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Không tìm thấy rạp phim.")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            List<Room> rooms = roomRepository.findAllByCinema(cinema.get());
            List<ShowTime> showTimeList = showTimeRepository.findAllByRoomIn(rooms);
            if (!showTimeList.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Phim đang được công chiếu, không thể xóa.")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            cinema.get().setStatus(!cinema.get().isStatus());

            cinemaRepository.save(cinema.get());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Cập nhật trạng thái rạp phim thành công!")
                            .result(cinema.get())
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> getCinema(String id) {
        try {
            Optional<Cinema> cinema = cinemaRepository.findById(id);
            return cinema.map(value -> ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy thông tin rạp phim thành công!")
                            .result(value)
                            .statusCode(HttpStatus.OK.value())
                            .build())).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Lấy thông tin rạp phim thất bại.")
                            .result(null)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .build()));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> getAllCinemaUnmanaged() {
        try {
            List<Cinema> cinemas = cinemaRepository.findAllByStatusIsTrue();
            List<Cinema> cinemaList = new ArrayList<>();
            for (Cinema item : cinemas) {
                Optional<Manager> manager = managerRepository.findByCinemaAndRole(item, roleService.findByRoleName("MANAGER"));
                if (manager.isEmpty()){
                    cinemaList.add(item);
                }
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy các rạp phim chưa có quản lý thành công!")
                            .result(cinemaList)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }
}
