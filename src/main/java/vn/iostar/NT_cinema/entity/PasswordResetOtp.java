package vn.iostar.NT_cinema.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

@Getter
@Setter
public class PasswordResetOtp implements Serializable {

    private static final int EXPIRATION = 5;

    @Id
    private String id;

    private String otp;

    private User user;

    private Date expiryDate;

    public PasswordResetOtp() {
        super();
    }

    public PasswordResetOtp(final String otp) {
        super();

        this.otp = otp;
        this.expiryDate = calculateExpiryDate();
    }

    public PasswordResetOtp(final String otp, final User user) {
        super();

        this.otp = otp;
        this.user = user;
        this.expiryDate = calculateExpiryDate();
    }


    private Date calculateExpiryDate() {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());
        cal.add(Calendar.MINUTE, PasswordResetOtp.EXPIRATION);
        return new Date(cal.getTime().getTime());
    }

    public void updateOtp(final String otp) {
        this.otp = otp;
        this.expiryDate = calculateExpiryDate();
    }

}
