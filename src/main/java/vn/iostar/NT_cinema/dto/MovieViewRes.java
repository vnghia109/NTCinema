package vn.iostar.NT_cinema.dto;

import lombok.*;
import vn.iostar.NT_cinema.entity.Genres;

import java.util.List;

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

    private List<Genres> genres;

    private String actor;

    private String desc;

    private String trailerLink;

    private String duration;
}
