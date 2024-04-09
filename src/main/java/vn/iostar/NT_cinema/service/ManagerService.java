package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.entity.Cinema;
import vn.iostar.NT_cinema.entity.Manager;
import vn.iostar.NT_cinema.repository.CinemaRepository;
import vn.iostar.NT_cinema.repository.ManagerRepository;
import vn.iostar.NT_cinema.repository.RoleRepository;

import java.util.*;

@Service
public class ManagerService {
    @Autowired
    ManagerRepository managerRepository;

    @Autowired
    CinemaRepository cinemaRepository;

    @Autowired
    RoleRepository roleRepository;
    public ResponseEntity<GenericResponse> getAllManager(Pageable pageable) {
        try {
            Page<Manager> managers = managerRepository.findAllByRole( roleRepository.findByRoleName("MANAGER"), pageable);

            Map<String, Object> map = new HashMap<>();
            map.put("content", managers.getContent());
            map.put("pageNumber", managers.getPageable().getPageNumber() + 1);
            map.put("pageSize", managers.getSize());
            map.put("totalPages", managers.getTotalPages());
            map.put("totalElements", managers.getTotalElements());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy tất cả quản lý thành công!")
                            .result(map)
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

    public ResponseEntity<GenericResponse> updateCinemaManager(String userId, String cinemaId) {
        try {
            Optional<Manager> manager = managerRepository.findById(userId);
            if (manager.isPresent() && manager.get().getRole().getRoleName().equals("MANAGER")){
                Optional<Cinema> cinema = cinemaRepository.findById(cinemaId);
                if (cinema.isPresent()){
                    manager.get().setCinema(cinema.get());
                    managerRepository.save(manager.get());
                }else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(GenericResponse.builder()
                                    .success(false)
                                    .message("Rạp phim không tìm thấy.")
                                    .result(null)
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .build());
                }
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Quản lý không tồn tại.")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Thêm rạp phim cho quản lý thành công!")
                            .result(manager.get())
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

    public ResponseEntity<GenericResponse> getManager(String id) {
        try {
            Optional<Manager> managerOptional = managerRepository.findById(id);
            return managerOptional.map(manager -> ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy thông tin quản lý thành công!")
                            .result(manager)
                            .statusCode(HttpStatus.OK.value())
                            .build())).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Quản lý không tồn tại.")
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
}
