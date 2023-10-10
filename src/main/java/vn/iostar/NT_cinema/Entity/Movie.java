package vn.iostar.NT_cinema.Entity;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
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

    private String actor;

    private String releaseDate;

    private String desc;

    private String poster;

    private String trailerLink;

    private List<Review> reviews;

    private List<ShowTime> showTimes;
}
