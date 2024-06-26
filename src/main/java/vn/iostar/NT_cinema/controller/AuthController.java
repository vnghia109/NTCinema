package vn.iostar.NT_cinema.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.*;
import vn.iostar.NT_cinema.entity.RefreshToken;
import vn.iostar.NT_cinema.entity.User;
import vn.iostar.NT_cinema.exception.UserNotFoundException;
import vn.iostar.NT_cinema.security.JwtTokenProvider;
import vn.iostar.NT_cinema.security.UserDetail;
import vn.iostar.NT_cinema.service.EmailVerificationService;
import vn.iostar.NT_cinema.service.RefreshTokenService;
import vn.iostar.NT_cinema.service.UserService;

import java.util.*;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    UserService userService;
    @Autowired
    RefreshTokenService refreshTokenService;
    @Autowired
    EmailVerificationService emailVerificationService;


    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        Optional<User> optionalUser = userService.findByUserName(loginDTO.getCredentialId());
        if (optionalUser.isEmpty()){
            throw new UserNotFoundException("Tài khoản không tồn tại");
        }
        if (!optionalUser.get().isActive()) {
            throw new DisabledException("Tài khoản của bạn chưa xác minh.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(optionalUser.get().getUserName(),
                        loginDTO.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetail userDetail = (UserDetail) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateAccessToken(userDetail);
        RefreshToken refreshToken = new RefreshToken();
        String token = jwtTokenProvider.generateRefreshToken(userDetail);
        refreshToken.setToken(token);
        refreshToken.setUser(userDetail.getUser());
        //invalid all refreshToken before
        refreshTokenService.revokeRefreshToken(userDetail.getUserId());
        refreshTokenService.save(refreshToken);
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", accessToken);
        tokenMap.put("refreshToken", token);

        optionalUser.get().setLastLoginAt(new Date());
        userService.save(optionalUser.get());

        return ResponseEntity.ok().body(GenericResponse.builder()
                .success(true)
                .message("Đăng nhập thành công!")
                .result(tokenMap)
                .statusCode(HttpStatus.OK.value())
                .build());

    }


    @PostMapping("/register")
    public ResponseEntity<GenericResponse> registerProcess(
            @Valid @RequestBody RegisterRequest registerRequest,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = Objects.requireNonNull(
                    bindingResult.getFieldError()).getDefaultMessage();

            throw new RuntimeException(errorMessage);
        }
        return userService.userRegister(registerRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader,
                                    @RequestParam("refreshToken") String refreshToken) {
        String accessToken = authorizationHeader.substring(7);
        if (jwtTokenProvider.getUserIdFromJwt(accessToken).equals(jwtTokenProvider.getUserIdFromRefreshToken(refreshToken))) {
            return refreshTokenService.logout(refreshToken);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GenericResponse.builder()
                        .success(false)
                        .message("Đăng xuất thất bại!")
                        .result("Vui lòng đăng nhập trước khi có thể đăng xuất!")
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .build());
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(@RequestHeader("Authorization") String authorizationHeader,
                                       @RequestParam("refreshToken") String refreshToken) {
        String accessToken = authorizationHeader.substring(7);
        if (jwtTokenProvider.getUserIdFromJwt(accessToken).equals(jwtTokenProvider.getUserIdFromRefreshToken(refreshToken))) {
            String userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);
            refreshTokenService.revokeRefreshToken(userId);
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Đăng xuất thành công!")
                    .result(null)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GenericResponse.builder()
                        .success(false)
                        .message("Đăng xuất thất bại!")
                        .result("Vui lòng đăng nhập trước khi có thể đăng xuất!")
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .build());
    }

    @PostMapping("/refresh-access-token")
    public ResponseEntity<?> refreshAccessToken(@RequestBody TokenRequest tokenRequest) {
        String refreshToken = tokenRequest.getRefreshToken();
        return refreshTokenService.refreshAccessToken(refreshToken);
    }

    @PostMapping("/sendOTP")
    public ResponseEntity<GenericResponse> sendOtp(@RequestBody EmailVerificationRequest emailVerificationRequest) {
        try {
            emailVerificationService.sendOtp(emailVerificationRequest.getEmail());
            return ResponseEntity.ok()
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Mã OTP đã được gửi thành công!")
                            .result(null)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Đã xảy ra lỗi khi gửi mã OTP.")
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    @PostMapping("/verifyOTP")
    public ResponseEntity<GenericResponse> verifyOtp(@RequestBody VerifyOtpRequest verifyOtpRequest) {
        boolean isOtpVerified = emailVerificationService.verifyOtp(verifyOtpRequest.getEmail(), verifyOtpRequest.getOtp());

        if (isOtpVerified) {
            return ResponseEntity.ok()
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Xác thực mã OTP thành công!!! Bạn có thể tiếp tục đăng nhập.")
                            .result(null)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } else {
            return ResponseEntity.badRequest()
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Mã OTP không hợp lệ hoặc đã hết hạn.")
                            .result(null)
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .build());
        }
    }
}
