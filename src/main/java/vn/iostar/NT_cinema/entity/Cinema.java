package vn.iostar.NT_cinema.entity;

import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "cinema")
public class Cinema {
    @Id
    private String cinemaId;

    private String location;

    @UniqueElements
    private String cinemaName;

    private String desc;

    private boolean status;

    private String urlLocation;
}
