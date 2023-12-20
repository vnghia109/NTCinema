package vn.iostar.NT_cinema.entity;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "RefreshToken")
public class RefreshToken {
    @Id
    private String id;

    @Size(max = 700)
    private String token;

    private boolean expired;
    private boolean revoked;

    private User user;
}
