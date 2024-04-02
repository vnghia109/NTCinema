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

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MovieReq {
    @NotBlank
    private String title;

    private String director;

    private String genres;

    @NotBlank
    private String actor;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date releaseDate;

    private String desc;

    @NotEmpty
    private List<MultipartFile> poster;

    @NotBlank
    private String trailerLink;

    @NotBlank
    @NotEmpty
    private String duration;
}
