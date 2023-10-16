package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.ManagerRequest;
import vn.iostar.NT_cinema.dto.RegisterRequest;
import vn.iostar.NT_cinema.entity.Role;
import vn.iostar.NT_cinema.entity.User;
import vn.iostar.NT_cinema.entity.Viewer;
import vn.iostar.NT_cinema.repository.UserRepository;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RoleService roleService;

    public Optional<User> findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    public <S extends User> S save(S entity) {
        return userRepository.save(entity);
    }

    public Optional<User> findByPassword(String password) {
        return userRepository.findByPassword(password);
    }

    public ResponseEntity<GenericResponse> userRegister(RegisterRequest registerRequest) {
        if (registerRequest.getPassword().length() < 8 || registerRequest.getPassword().length() > 32)
            throw new RuntimeException("Password must be between 8 and 32 characters long");

        Optional<User> userOptional = userRepository.findByPhone(registerRequest.getPhone());
        if (userOptional.isPresent())
            return ResponseEntity.status(409)
                    .body(
                            GenericResponse.builder()
                                    .success(true)
                                    .message("Phone number already in use")
                                    .result(null)
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .build()
                    );

        userOptional = userRepository.findByEmail(registerRequest.getEmail());
        if (userOptional.isPresent())
            return ResponseEntity.status(409)
                    .body(
                            GenericResponse.builder()
                                    .success(true)
                                    .message("Email already in use")
                                    .result(null)
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .build()
                    );

        userOptional = userRepository.findByUserName(registerRequest.getUserName());
        if (userOptional.isPresent())
            return ResponseEntity.status(409)
                    .body(
                            GenericResponse.builder()
                                    .success(true)
                                    .message("User name already in use")
                                    .result(null)
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .build()
                    );

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword()))
            return ResponseEntity.status(409)
                    .body(
                            GenericResponse.builder()
                                    .success(true)
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

//        emailVerificationService.sendOtp(registerRequest.getEmail());

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .success(true)
                        .message("Sign Up Success")
                        .result(null)
                        .statusCode(200)
                        .build()
        );
    }

    public ResponseEntity<GenericResponse> addManager(ManagerRequest request) {
        try {
            User user = new User();
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
            user.setUserName(request.getUserName());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setFullName(request.getFullName());
            user.setCreatedAt(new Date());
            user.setPhone(request.getPhone());
            user.setRole(roleService.findByRoleName("MANAGER"));

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