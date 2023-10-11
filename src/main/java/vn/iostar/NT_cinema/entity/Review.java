package vn.iostar.NT_cinema.entity;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Document(collection = "review")
public class Review {

    @Id
    private String reviewId;

    @DBRef
    private Movie movie;

    @DBRef
    private User user;

    private String comment;

    @Size( min = 0, max = 5)
    private int rating;

}
