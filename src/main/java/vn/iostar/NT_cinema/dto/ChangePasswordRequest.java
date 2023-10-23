package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    @NotBlank(message = "Current password cannot be blank")
    String currentPassword;
    @NotBlank(message = "New password cannot be blank")
    String newPassword;
    @NotBlank(message = "Confirm new password cannot be blank")
    String confirmNewPassword;
}
