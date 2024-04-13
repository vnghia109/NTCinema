package vn.iostar.NT_cinema.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "promotion")
public class Promotion {
    @Id
    private String promotionId;
    @DBRef
    private Cinema cinema;
    private String title;
    private String description;
    private double discount;
    private LocalDate startDate;
    private LocalDate endDate;
}
