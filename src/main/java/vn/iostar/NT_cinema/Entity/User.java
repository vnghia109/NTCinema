package vn.iostar.NT_cinema.Entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "user")
public class User {
    @Id
    private String userId;

    private Address address;

    private Role role;

    @NotBlank
    @UniqueElements
    private String userName;

    @NotBlank
    private String password;

    @NotBlank
    @Email
    private String email;

    private String fullName;

    private Date dob;
    @Size(max = 10)
    private String phone;

    private List<Booking> bookings;
}
