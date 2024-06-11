package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.entity.Genres;
import vn.iostar.NT_cinema.repository.GenresRepository;

import java.util.Optional;

@Service
public class GenresService {
    @Autowired
    GenresRepository genresRepository;

    public ResponseEntity<GenericResponse> createGenres(String name) {
        try {
            Optional<Genres> optionalGenres = genresRepository.findByName(name);
            if (optionalGenres.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Thể loại phim đã tồn tại.")
                                .result(null)
                                .statusCode(HttpStatus.CONFLICT.value())
                                .build());
            }
            Genres genres = new Genres();
            genres.setName(name);
            Genres genresRes = genresRepository.save(genres);
            return ResponseEntity.ok()
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Tạo thể loại phim thành công!")
                            .result(genresRes)
                            .statusCode(HttpStatus.OK.value())
                            .build());
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

    public ResponseEntity<GenericResponse> deleteGenres(String id) {
        try {
            genresRepository.deleteById(id);
            return ResponseEntity.ok()
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Xóa thể loại phim thành công!")
                            .result(null)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ. Xóa thất bại.")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }
}