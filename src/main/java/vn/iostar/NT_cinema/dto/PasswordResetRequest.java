package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetRequest {
    @NotNull
    @NotBlank
    @Size(min = 8, max = 32, message = "Password must be between 8 and 32 characters long")
    private String newPassword;

    @NotNull
    @NotBlank
    private String confirmPassword;
}
