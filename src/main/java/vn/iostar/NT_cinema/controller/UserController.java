package vn.iostar.NT_cinema.controller;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.ChangePasswordRequest;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.PasswordResetRequest;
import vn.iostar.NT_cinema.dto.UserReq;
import vn.iostar.NT_cinema.security.JwtTokenProvider;
import vn.iostar.NT_cinema.service.UserService;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserService userService;

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
                                                          BindingResult bindingResult) throws Exception {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        if (bindingResult.hasErrors()) {
            throw new RuntimeException(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }

        return userService.changePassword(userId, request);
    }

    @PutMapping("/update")
    public ResponseEntity<GenericResponse> updateProfile(@RequestBody UserReq req,
                                                         @RequestHeader("Authorization") String authorizationHeader){
        if (authorizationHeader.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GenericResponse.builder()
                            .success(false)
                            .message("Access Denied")
                            .result(null)
                            .statusCode(401)
                            .build()
            );
        }
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return userService.updateUser(req, userId);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<GenericResponse> resetPassword(@RequestParam final String email) {
        return userService.forgotPassword(email);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String token,@RequestBody PasswordResetRequest passwordResetRequest){
        return userService.resetPassword(token, passwordResetRequest);
    }

}
