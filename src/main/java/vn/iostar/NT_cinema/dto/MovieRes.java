package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.Id;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MovieRes {
    @Id
    private String movieId;

    @NotBlank
    @UniqueElements
    private String title;

    private String director;

    private String genres;

    @NotBlank
    private String actor;

    @NotBlank
    private String releaseDate;

    private String desc;

    @NotBlank
    private String poster;

    @NotBlank
    private String trailerLink;

    private String duration;

    private String rating;
}
