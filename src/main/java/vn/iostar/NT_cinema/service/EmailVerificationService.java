package vn.iostar.NT_cinema.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vn.iostar.NT_cinema.entity.EmailVerification;
import vn.iostar.NT_cinema.entity.User;
import vn.iostar.NT_cinema.repository.EmailVerificationRepository;
import vn.iostar.NT_cinema.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Service
public class EmailVerificationService {
    @Autowired
    EmailVerificationRepository emailVerificationRepository;

    @Autowired
    JavaMailSender mailSender;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TemplateEngine templateEngine;

    @Autowired
    Environment env;

    public void sendOtp(String email) {
        String otp = generateOtp();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);

            // Load Thymeleaf template
            Context context = new Context();
            context.setVariable("otpCode", otp);
            context.setVariable("verifyEmail", email);
            String mailContent = templateEngine.process("send-otp", context);

            helper.setText(mailContent, true);
            helper.setSubject("The verification token for TNCinemas");
            helper.setFrom(Objects.requireNonNull(env.getProperty("spring.mail.username")),"TNCinemas Admin");
            mailSender.send(message);

            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
            EmailVerification emailVerification = new EmailVerification();
            emailVerification.setEmail(email);
            emailVerification.setOtp(otp);
            emailVerification.setExpirationTime(expirationTime);

            Optional<EmailVerification> existingEmailVerification = emailVerificationRepository.findByEmail(email);
            existingEmailVerification.ifPresent(verification -> emailVerificationRepository.delete(verification));

            emailVerificationRepository.save(emailVerification);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean verifyOtp(String email, String otp) {
        Optional<EmailVerification> emailVerification = emailVerificationRepository.findByEmail(email);
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent() && emailVerification.isPresent() && emailVerification.get().getOtp().equals(otp)) {
            User user = optionalUser.get();
            user.setActive(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        Random random = new Random();
        int OTP_LENGTH = 6;
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    public void deleteExpiredOtp() {
        LocalDateTime now = LocalDateTime.now();
        List<EmailVerification> expiredOtpList = emailVerificationRepository.findByExpirationTimeBefore(now);
        emailVerificationRepository.deleteAll(expiredOtpList);
    }

    @Scheduled(fixedDelay = 30000) // 5 minutes
    public void cleanupExpiredOtp() {
        deleteExpiredOtp();
    }
}
