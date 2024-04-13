package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StaffReq {
    @NotBlank(message = "Tên đăng nhập không được để trống.")
    @NotEmpty(message = "Tên đăng nhập không được để trống.")
    private String userName;

    @NotBlank(message = "Mật khẩu không được để trống.")
    @NotEmpty(message = "Mật khẩu không được để trống.")
    private String password;

    @NotBlank(message = "Email không được để trống.")
    @NotEmpty(message = "Email không được để trống.")
    @Email(message = "Email không đúng định dạng.")
    private String email;

    private String fullName;

    @Size(max = 10, message = "SDT không đúng định dạng.")
    private String phone;
}
