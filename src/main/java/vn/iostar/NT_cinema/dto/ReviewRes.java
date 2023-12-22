package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRes {
    private String movie;

    private String user;

    private String comment;

    @Size(min = 0, max = 5)
    private int rating;
}
