package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.Id;
import vn.iostar.NT_cinema.entity.Genres;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MovieRes {
    private String movieId;

    @NotBlank
    @UniqueElements
    private String title;

    private String director;

    private List<Genres> genres;

    @NotBlank
    private String actor;

    @NotBlank
    private Date releaseDate;

    private String desc;

    @NotBlank
    private String poster;

    @NotBlank
    private String trailerLink;

    private String duration;

    private String rating;
}
