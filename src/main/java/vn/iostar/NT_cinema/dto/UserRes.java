package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import vn.iostar.NT_cinema.entity.User;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRes {
    private String userId;

    private String avatar;

    private String userName;

    private String email;

    private String fullName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date dob;

    @Size(max = 10)
    private String phone;

    private String street;
    private String province;
    private String district;
    private String country;

    public UserRes(User user) {
        this.userId = user.getUserId();
        this.avatar = user.getAvatar();
        this.userName = user.getUserName();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.dob = user.getDob();
        this.phone = user.getPhone();
        if (user.getAddress() != null) {
            this.street = user.getAddress().getStreet();
            this.province = user.getAddress().getProvince();
            this.district = user.getAddress().getDistrict();
            this.country = user.getAddress().getCountry();
        }else {
            this.street = null;
            this.province = null;
            this.district = null;
            this.country = null;
        }
    }
}
