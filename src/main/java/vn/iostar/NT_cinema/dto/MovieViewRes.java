package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MovieViewRes {
    private String movieId;

    private String title;

    private String poster;

    private String slider;

    private String rating;

    private String director;

    private String genres;

    private String actor;

    private String desc;

    private String trailerLink;

    private String duration;
}
