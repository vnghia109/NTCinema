package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ManagerRequest {

    @NotBlank
    @NotEmpty
    private String userName;

    @NotBlank
    @NotEmpty
    private String password;

    @NotBlank
    @NotEmpty
    @Email
    private String email;

    private String fullName;

    @Size(max = 10)
    private String phone;
}
