package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.UniqueElements;
import vn.iostar.NT_cinema.entity.Address;
import vn.iostar.NT_cinema.entity.Role;

import java.util.Date;

@Data
public class UpdateUserReq {

    private String role;

    @NotBlank
    @NotEmpty
    private String userName;

    @NotBlank
    @NotEmpty
    @Email
    private String email;

    private String fullName;

    private Date dob;

    @Size(max = 10)
    private String phone;

    private String street;
    private String province;
    private String district;
    private String country;
}
