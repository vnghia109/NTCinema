package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import vn.iostar.NT_cinema.entity.Address;

import java.util.Date;

@Data
public class UserReq {
    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phone;

    private String email;

    private Date dob;

    private Address address;
}
