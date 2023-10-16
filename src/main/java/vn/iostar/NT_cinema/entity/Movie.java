package vn.iostar.NT_cinema.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "movie")
public class Movie {

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

    private List<Review> reviews;

    private List<ShowTime> showTimes;

    public Movie(String title, String director, String genres, String actor, String releaseDate, String desc, String poster, String trailerLink, List<Review> reviews, List<ShowTime> showTimes) {
        this.title = title;
        this.director = director;
        this.genres = genres;
        this.actor = actor;
        this.releaseDate = releaseDate;
        this.desc = desc;
        this.poster = poster;
        this.trailerLink = trailerLink;
        this.reviews = reviews;
        this.showTimes = showTimes;
    }
}