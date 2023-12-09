package vn.iostar.NT_cinema.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vn.iostar.NT_cinema.dto.*;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.AddressRepository;
import vn.iostar.NT_cinema.repository.CinemaRepository;
import vn.iostar.NT_cinema.repository.PasswordResetOtpRepository;
import vn.iostar.NT_cinema.repository.UserRepository;

import java.util.*;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RoleService roleService;
    @Autowired
    CinemaRepository cinemaRepository;
    @Autowired
    EmailVerificationService emailVerificationService;
    @Autowired
    PasswordResetOtpRepository passwordResetOtpRepository;
    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    Environment env;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    AddressRepository addressRepository;
//    @Autowired
//    TemplateEngine templateEngine;

    public Optional<User> findByUserName(String userName) {
        Optional<User> user = userRepository.findByUserName(userName);
        if (user.isPresent()){
            return user;
        }
        return userRepository.findByEmail(userName);
    }

    public <S extends User> S save(S entity) {
        return userRepository.save(entity);
    }

    public Optional<User> findByPassword(String password) {
        return userRepository.findByPassword(password);
    }

    public ResponseEntity<GenericResponse> userRegister(RegisterRequest registerRequest) {
        Optional<User> userOptional = userRepository.findByEmail(registerRequest.getEmail());
        if (userOptional.isPresent())
            return ResponseEntity.status(409)
                    .body(
                            GenericResponse.builder()
                                    .success(false)
                                    .message("Email already in use")
                                    .result(null)
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .build()
                    );

        userOptional = userRepository.findByPhone(registerRequest.getPhone());
        if (userOptional.isPresent())
            return ResponseEntity.status(409)
                    .body(
                            GenericResponse.builder()
                                    .success(false)
                                    .message("Phone number already in use")
                                    .result(null)
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .build()
                    );

        userOptional = userRepository.findByUserName(registerRequest.getUserName());
        if (userOptional.isPresent())
            return ResponseEntity.status(409)
                    .body(
                            GenericResponse.builder()
                                    .success(false)
                                    .message("User name already in use")
                                    .result(null)
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .build()
                    );

        if (registerRequest.getPassword().length() < 8 || registerRequest.getPassword().length() > 32)
            throw new RuntimeException("Password must be between 8 and 32 characters long");

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword()))
            return ResponseEntity.status(409)
                    .body(
                            GenericResponse.builder()
                                    .success(false)
                                    .message("Password and confirm password do not match")
                                    .result(null)
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .build()
                    );

        User user = new Viewer();
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setCreatedAt(new Date());
        user.setUserName(registerRequest.getUserName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(roleService.findByRoleName("VIEWER"));

        save(user);

        emailVerificationService.sendOtp(registerRequest.getEmail());

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .success(true)
                        .message("Registration initiated. Please verify OTP.")
                        .result(null)
                        .statusCode(200)
                        .build()
        );
    }

    public ResponseEntity<GenericResponse> addManager(ManagerRequest request) {
        try {
            Manager user = new Manager();
            if (userRepository.findByUserName(request.getUserName()).isPresent()){
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(
                                GenericResponse.builder()
                                        .success(false)
                                        .message("User name already in use")
                                        .result(null)
                                        .statusCode(HttpStatus.CONFLICT.value())
                                        .build()
                        );
            }
            if (userRepository.findByPhone(request.getPhone()).isPresent()){
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(
                                GenericResponse.builder()
                                        .success(false)
                                        .message("Phone already in use")
                                        .result(null)
                                        .statusCode(HttpStatus.CONFLICT.value())
                                        .build()
                        );
            }
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(
                                GenericResponse.builder()
                                        .success(false)
                                        .message("Email already in use")
                                        .result(null)
                                        .statusCode(HttpStatus.CONFLICT.value())
                                        .build()
                        );
            }

            Optional<Cinema> cinema = cinemaRepository.findById(request.getCinemaId());
            if (cinema.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Cinema not found")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }

            user.setUserName(request.getUserName());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setFullName(request.getFullName());
            user.setCreatedAt(new Date());
            user.setPhone(request.getPhone());
            user.setRole(roleService.findByRoleName("MANAGER"));
            user.setActive(true);
            user.setCinema(cinema.get());

            User manager = save(user);

            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("SignUp to manager success")
                            .result(manager)
                            .statusCode(200)
                            .build()
            );

        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            GenericResponse.builder()
                                    .success(false)
                                    .message(e.getMessage())
                                    .result(null)
                                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                    .build()
                    );
        }
    }

    public ResponseEntity<GenericResponse> getProfile(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty())
            throw new RuntimeException("User not found");

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .success(true)
                        .message("Retrieving user profile successfully")
                        .result(user.get())
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    public ResponseEntity<GenericResponse> changePassword(String userId, ChangePasswordRequest request) {
        try {

            if (request.getNewPassword().length() < 8 || request.getNewPassword().length() > 32)
                return ResponseEntity.status(409)
                        .body(
                                GenericResponse.builder()
                                        .success(false)
                                        .message("Password must be between 8 and 32 characters long")
                                        .result(null)
                                        .statusCode(HttpStatus.CONFLICT.value())
                                        .build()
                        );

            if (!request.getNewPassword().equals(request.getConfirmNewPassword()))
                return ResponseEntity.status(409)
                        .body(
                                GenericResponse.builder()
                                        .success(false)
                                        .message("Password and confirm password do not match")
                                        .result(null)
                                        .statusCode(HttpStatus.CONFLICT.value())
                                        .build()
                        );

            Optional<User> userOptional = userRepository.findById(userId);

            if (userOptional.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(
                                GenericResponse.builder()
                                        .success(false)
                                        .message("This account is not found")
                                        .result(null)
                                        .statusCode(HttpStatus.NOT_FOUND.value())
                                        .build()
                        );

            User user = userOptional.get();
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword()))
                return ResponseEntity.status(409)
                    .body(
                            GenericResponse.builder()
                                    .success(false)
                                    .message("Current password is incorrect")
                                    .result(null)
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .build()
                    );

            if (passwordEncoder.matches(request.getNewPassword(), user.getPassword()))
                return ResponseEntity.status(409)
                        .body(
                                GenericResponse.builder()
                                        .success(false)
                                        .message("The new password cannot be the same as the old password")
                                        .result(null)
                                        .statusCode(HttpStatus.CONFLICT.value())
                                        .build()
                        );

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            save(user);

            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Change password successful")
                            .result(null)
                            .statusCode(200)
                            .build()
            );
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            GenericResponse.builder()
                                    .success(false)
                                    .message(e.getMessage())
                                    .result(null)
                                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                    .build()
                    );
        }

    }

    public ResponseEntity<GenericResponse> updateUser(UserReq request, String userId) {
        try {
            Optional<User> optionalViewer = userRepository.findById(userId);
            if (optionalViewer.isPresent()){
                User user = optionalViewer.get();

                if (request.getPhone() != null) {
                    user.setPhone(request.getPhone());
                }
                if (request.getFullName() != null) {
                    user.setFullName(request.getFullName());
                }
                if (request.getAddress() != null){
                    user.setAddress(request.getAddress());
                }
                if (request.getEmail() != null) {
                    user.setEmail(request.getEmail());
                }
                if (request.getDob() != null){
                    user.setDob(request.getDob());
                }

                user.setUpdatedAt(new Date());

                userRepository.save(user);

                return ResponseEntity.ok().body(GenericResponse.builder()
                        .success(true)
                        .message("Update success")
                        .result(user)
                        .statusCode(200)
                        .build());
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Update fail")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public void createPasswordResetOtpForUser(User user, String otp) {
        PasswordResetOtp myOtp = null;
        if (passwordResetOtpRepository.findByUser(user).isPresent()) {
            myOtp = (PasswordResetOtp) passwordResetOtpRepository.findByUser(user).get();
            myOtp.updateOtp(otp);
        } else {

            myOtp = new PasswordResetOtp(otp, user);
        }
        passwordResetOtpRepository.save(myOtp);
    }

    public ResponseEntity<GenericResponse> forgotPassword(String email){
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                    .success(true)
                    .message("User not found")
                    .result(null)
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .build());
        try {

            String otp = emailVerificationService.generateOtp();
            createPasswordResetOtpForUser(user.get(), otp);
            String subject = "Change Password For TNCinemas";
            Context context = new Context();
            context.setVariable("otpCode",otp);
            String content = templateEngine.process("forgot-password",context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,true);
            helper.setSubject(subject);
            helper.setText(content,true);
            helper.setTo(user.get().getEmail());
            helper.setFrom(Objects.requireNonNull(env.getProperty("spring.mail.username")),"TNCinemas Admin");

            javaMailSender.send(message);

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Please check your email to reset your password!")
                    .result("Send Otp successfully!")
                    .statusCode(HttpStatus.OK.value())
                    .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    @Transactional
    public void deleteUnverifiedAccounts() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        Date twentyFourHoursAgo = calendar.getTime();

        List<User> unverifiedAccounts = userRepository.findByIsActiveFalseAndCreatedAtBefore(twentyFourHoursAgo);
        userRepository.deleteAll(unverifiedAccounts);
    }
//    @PostConstruct
//    public void init() {
//        deleteUnverifiedAccounts();
//    }
//    @Scheduled(fixedDelay = 86400000) // 24 hours
//    public void scheduledDeleteUnverifiedAccounts() {
//        deleteUnverifiedAccounts();
//    }

    public ResponseEntity<?> validateOtp(String token) {
        try {
            String result = validatePasswordResetOtp(token);
            if (result == null){
                return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                        .success(true)
                        .message("Validate otp success.")
                        .result(null)
                        .statusCode(HttpStatus.OK.value())
                        .build());
            }else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                        .success(false)
                        .message(result)
                        .result(null)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build());
            }
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> resetPassword(String token, PasswordResetRequest passwordResetRequest) {
        try {
                Optional<PasswordResetOtp> user = passwordResetOtpRepository.findByOtp(token);
                if (user.isEmpty()){
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                            GenericResponse.builder()
                                    .success(false)
                                    .message("User Not Found")
                                    .result(null)
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .build());
                }
                changeUserPassword(user.get().getUser(), passwordResetRequest.getNewPassword()
                        , passwordResetRequest.getConfirmPassword());
                return ResponseEntity.ok(
                        GenericResponse.builder()
                                .success(true)
                                .message("Reset password successful")
                                .result(null)
                                .statusCode(200)
                                .build()
                );

        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public void changeUserPassword(User user, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword))
            throw new RuntimeException("Password and confirm password do not match");
        user.setPassword(passwordEncoder.encode(newPassword));
        save(user);
    }

    public String validatePasswordResetOtp(String otp) {

        Optional<PasswordResetOtp> passOtp = passwordResetOtpRepository.findByOtp(otp);
        Calendar cal = Calendar.getInstance();

        if (passOtp.isEmpty()) {
            return "Invalid token/link";
        }
        if (passOtp.get().getExpiryDate().before(cal.getTime())) {
            return "Token/link expired";
        }
        return null;
    }

    public ResponseEntity<GenericResponse> getAllUser(PageRequest pageRequest) {
        try {
            Page<User> users = userRepository.findAll(pageRequest);

            Map<String, Object> map = new HashMap<>();
            map.put("content", users.getContent());
            map.put("pageNumber", users.getPageable().getPageNumber() + 1);
            map.put("pageSize", users.getSize());
            map.put("totalPages", users.getTotalPages());
            map.put("totalElements", users.getTotalElements());

            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Get all user success")
                            .result(map)
                            .statusCode(200)
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> getUser(String id) {
        try {
            Optional<User> user = userRepository.findById(id);

            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Get user success")
                            .result(user.get())
                            .statusCode(200)
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> adminUpdateUser(String userId, UpdateUserReq request) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();

                user.setPhone(request.getPhone());
                user.setFullName(request.getFullName());
                user.setEmail(request.getEmail());
                user.setDob(request.getDob());
                user.setRole(roleService.findByRoleName(request.getRole()));
                user.setUserName(request.getUserName());
                user.setUpdatedAt(new Date());
                Optional<Address> optionalAddress = addressRepository.findByStreetAndProvinceAndDistrictAndCountry(
                        request.getAddress().getStreet(),
                        request.getAddress().getProvince(),
                        request.getAddress().getDistrict(),
                        request.getAddress().getCountry());
                if (optionalAddress.isPresent()){
                    user.setAddress(optionalAddress.get());
                }else {
                    Address address = addressRepository.save(request.getAddress());
                    user.setAddress(address);
                }

                userRepository.save(user);

                return ResponseEntity.ok().body(GenericResponse.builder()
                        .success(true)
                        .message("Update success")
                        .result(user)
                        .statusCode(200)
                        .build());
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Update fail")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> deleteUser(String id) {
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()){
                user.get().setDelete(true);
                userRepository.save(user.get());
                return ResponseEntity.ok().body(GenericResponse.builder()
                        .success(true)
                        .message("Update success")
                        .result(user.get())
                        .statusCode(200)
                        .build());
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("User not found")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }

        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

//    public String validateVerificationAccount(String token) {
//
//        VerificationToken verificationToken = tokenRepository.findByToken(token);
//        if (verificationToken == null) {
//            return "Invalid token, please check the token again!";
//        }
//        User user = verificationToken.getUser();
//        user.setVerified(true);
//        userRepository.save(user);
//        return "Account verification successful, please login!";
//    }
}
