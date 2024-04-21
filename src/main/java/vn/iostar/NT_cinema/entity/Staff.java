package vn.iostar.NT_cinema.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Staff extends User {
    @DBRef
    private Cinema cinema;
}
