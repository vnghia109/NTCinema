package vn.iostar.NT_cinema.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "user")
public class User {
    @Id
    private String userId;

    private String avatar;

    @DBRef
    private Address address;

    @DBRef
    private Role role;

    @NotBlank
    @NotEmpty
    @UniqueElements
    private String userName;

    @NotBlank
    @NotEmpty
    private String password;

    @NotBlank
    @NotEmpty
    @Email
    @UniqueElements
    private String email;

    private String fullName;

    private Date dob;

    @Size(max = 10)
    @UniqueElements
    private String phone;

    private boolean isActive = false;

    private boolean isDelete = false;

    private Date createdAt;

    private Date updatedAt;

    private Date lastLoginAt;

}
