package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.repository.PasswordResetOtpRepository;

@Service
public class PasswordResetOtpService {
    @Autowired
    PasswordResetOtpRepository passwordResetOtpRepository;
}
