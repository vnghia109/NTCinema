package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateMovieReq {
    private String title;

    private String director;

    private List<String> genres;

    private String actor;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date releaseDate;

    private String desc;

    private List<MultipartFile> poster;

    private List<MultipartFile> slider;

    private String trailerLink;

    private String duration;
}
