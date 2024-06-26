package vn.iostar.NT_cinema.entity;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Document(collection = "review")
public class Review {

    @Id
    private String reviewId;

    private String movieId;

    private String movieName;

    private String userName;

    private String comment;

    private Date createAt;

    @Size(min = 0, max = 5)
    private int rating;

}
