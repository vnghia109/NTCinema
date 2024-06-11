package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import vn.iostar.NT_cinema.entity.Genres;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MovieReq {
    @NotBlank(message = "Tên phim không được để trống")
    @NotEmpty(message = "Tên phim không được để trống")
    private String title;

    private String director;

    private List<String> genres;

    @NotBlank(message = "Diễn viên không được để trống")
    @NotEmpty(message = "Diễn viên không được để trống")
    private String actor;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date releaseDate;

    private String desc;

    @NotEmpty(message = "Hình ảnh phim phải được thêm")
    private List<MultipartFile> poster;

    private List<MultipartFile> slider;

    @NotBlank(message = "Trailer phim phải được thêm")
    @NotEmpty(message = "Trailer phim phải được thêm")
    private String trailerLink;

    @NotBlank(message = "Thời lượng phim không được để trống")
    @NotEmpty(message = "Thời lượng phim không được để trống")
    private String duration;
}
