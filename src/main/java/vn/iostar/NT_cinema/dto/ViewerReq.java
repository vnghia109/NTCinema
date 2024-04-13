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
public class ViewerReq {
    @Email(message = "Email không đúng định dạng.")
    private String email;

    private String fullName;

    @Size(max = 10, message = "SDT không đúng định dạng.")
    private String phone;

    @Size(min = 8, max = 32, message = "Mật này từ 8-32 kí tự.")
    @NotBlank(message = "Mật khẩu không được để trống.")
    @NotEmpty(message = "Mật khẩu không được để trống.")
    private String password;
}
