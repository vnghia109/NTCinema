package vn.iostar.NT_cinema.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.*;
import vn.iostar.NT_cinema.exception.AlreadyExistException;
import vn.iostar.NT_cinema.repository.UserTokenRepository;
import vn.iostar.NT_cinema.security.JwtTokenProvider;
import vn.iostar.NT_cinema.service.NotificationService;
import vn.iostar.NT_cinema.service.UserService;
import vn.iostar.NT_cinema.entity.UserTokenFCM;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    UserTokenRepository userTokenRepository;
    @Autowired
    UserService userService;
    @Autowired
    NotificationService notificationService;

    @GetMapping("/profile")
    public ResponseEntity<GenericResponse> getInformation(
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return userService.getProfile(userId);
    }

    @PutMapping("/change-password")
    public ResponseEntity<GenericResponse> changePassword(@RequestBody @Valid ChangePasswordRequest request,
                                                          @RequestHeader("Authorization") String authorizationHeader,
                                                          BindingResult bindingResult) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        if (bindingResult.hasErrors()) {
            throw new RuntimeException(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }

        return userService.changePassword(userId, request);
    }

    @PutMapping("/update")
    public ResponseEntity<GenericResponse> updateProfile(@Valid @ModelAttribute UserReq req,
                                                         @RequestHeader("Authorization") String authorizationHeader,
                                                         BindingResult bindingResult) {
        if (authorizationHeader.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GenericResponse.builder()
                            .success(false)
                            .message("Từ chối truy cập")
                            .result(null)
                            .statusCode(401)
                            .build()
            );
        }
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new GenericResponse(
                    false,
                    "Dữ liệu đầu vào không đúng định dạng!",
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(),
                    HttpStatus.BAD_REQUEST.value()));
        }
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return userService.updateUser(req, userId);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<GenericResponse> forgotPassword(@RequestParam final String email) {
        return userService.forgotPassword(email);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String token,@RequestBody PasswordResetRequest passwordResetRequest){
        return userService.resetPassword(token, passwordResetRequest);
    }

    @PutMapping("/valid-otp")
    public ResponseEntity<?> validateOtp(@RequestParam("token") String token){
        return userService.validateOtp(token);
    }


    @GetMapping("/notifications")
    public ResponseEntity<GenericResponse> getNotifications(@RequestHeader("Authorization") String authorizationHeader,
                                                            @RequestParam(defaultValue = "1") int index,
                                                            @RequestParam(defaultValue = "10") int size) {
        String userId = jwtTokenProvider.getUserIdFromJwt(
                authorizationHeader.substring(7)
        );
        return notificationService.getNotifications(userId, PageRequest.of(index-1, size));
    }

    @PostMapping("/notification/read-all")
    public ResponseEntity<GenericResponse> readAllNotification(@RequestHeader("Authorization") String authorizationHeader) {
        String userId = jwtTokenProvider.getUserIdFromJwt(
                authorizationHeader.substring(7)
        );
        return notificationService.readAllNotification(userId);
    }

    @GetMapping("/notification/{notificationUserId}")
    public ResponseEntity<GenericResponse> getNotification(@PathVariable("notificationUserId") String notificationUserId) {
        return notificationService.getNotification(notificationUserId);
    }

    @GetMapping("/notifications/notRead")
    public ResponseEntity<GenericResponse> notReadCount(@RequestHeader("Authorization") String authorizationHeader) {
        String userId = jwtTokenProvider.getUserIdFromJwt(
                authorizationHeader.substring(7)
        );
        return notificationService.notReadCount(userId);
    }

    @PostMapping("/save-token")
    public ResponseEntity<GenericResponse> saveToken(@RequestHeader("Authorization") String authorizationHeader,
                                                     @RequestBody UserTokenReq tokenRequest) throws RuntimeException {
        String userId = jwtTokenProvider.getUserIdFromJwt(
                authorizationHeader.substring(7)
        );
        Optional<UserTokenFCM> token = userTokenRepository.findByUserId(userId);
        if (token.isPresent()) {
            throw new AlreadyExistException("Token đã tồn tại.");
        }
        UserTokenFCM userTokenFCM = new UserTokenFCM();
        userTokenFCM.setUserId(userId);
        userTokenFCM.setToken(tokenRequest.getToken());
        userTokenRepository.save(userTokenFCM);
        return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                .success(true)
                .message("Đã cho phép thông báo!")
                .result(null)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/check/token")
    public ResponseEntity<GenericResponse> checkToken(@RequestHeader("Authorization") String authorizationHeader) throws RuntimeException {
        String userId = jwtTokenProvider.getUserIdFromJwt(
                authorizationHeader.substring(7)
        );
        Optional<UserTokenFCM> token = userTokenRepository.findByUserId(userId);
        if (token.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Đã cho phép thông báo!")
                    .result(true)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Chưa cho phép thông báo!")
                    .result(false)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        }
    }
}
