package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import vn.iostar.NT_cinema.entity.Address;

import java.util.Date;
import java.util.List;

@Data
public class UserReq {
    private String fullName;

    private String userName;

    private List<MultipartFile> image;

    @Size(max = 10, message = "Số điện thoại phải ít hơn 10 số")
    private String phone;

    @Email(message = "Email không đúng định dạng")
    private String email;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date dob;

    private String street;
    private String province;
    private String district;
    private String country;
}
