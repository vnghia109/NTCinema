package vn.iostar.NT_cinema.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
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

    @DBRef
    private List<Genres> genres = new ArrayList<>();

    @NotBlank
    private String actor;

    private Date releaseDate;

    private String desc;

    @NotBlank
    private String poster;

    private String slider;

    @NotBlank
    private String trailerLink;

    private String duration;

    private boolean isDelete = false;

    @DBRef
    private List<Review> reviews = new ArrayList<>();

    private String rating;


    public Movie(String title, String director, List<Genres> genres, String actor, Date releaseDate, String desc, String trailerLink, String duration) {
        this.title = title;
        this.director = director;
        this.genres = genres;
        this.actor = actor;
        this.releaseDate = releaseDate;
        this.desc = desc;
        this.trailerLink = trailerLink;
        this.duration = duration;
    }

    public void addReview(Review review) {
        this.reviews.add(review);
        updateRating();
    }

    private void updateRating() {
        if (this.reviews.isEmpty()) {
            this.rating = "0";
        } else {
            int totalRating = 0;
            for (Review review : this.reviews) {
                totalRating += review.getRating();
            }
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            float rating = (float) totalRating / this.reviews.size();
            this.rating = decimalFormat.format(rating);
        }
    }
}
