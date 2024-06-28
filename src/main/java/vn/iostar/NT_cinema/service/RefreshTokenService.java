package vn.iostar.NT_cinema.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.entity.RefreshToken;
import vn.iostar.NT_cinema.entity.User;
import vn.iostar.NT_cinema.repository.RefreshTokenRepository;
import vn.iostar.NT_cinema.repository.UserRepository;
import vn.iostar.NT_cinema.security.JwtTokenProvider;
import vn.iostar.NT_cinema.security.UserDetail;
import vn.iostar.NT_cinema.security.UserDetailServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RefreshTokenService {
    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserDetailServiceImpl userDetailsService;

    public void revokeRefreshToken(String userId) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent() && optionalUser.get().isActive()) {
                List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByUser_UserIdAndExpiredIsFalseAndRevokedIsFalse(userId);
                if (refreshTokens.isEmpty()) {
                    return;
                }
                refreshTokens.forEach(token -> {
                    token.setRevoked(true);
                    token.setExpired(true);
                });
                refreshTokenRepository.saveAll(refreshTokens);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <S extends RefreshToken> S save(S entity) {
        return refreshTokenRepository.save(entity);
    }

    public ResponseEntity<?> logout(String refreshToken) {
        try {
            if (jwtTokenProvider.validateToken(refreshToken)) {
                Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByTokenAndExpiredIsFalseAndRevokedIsFalse(refreshToken);
                if (optionalRefreshToken.isPresent()) {
                    optionalRefreshToken.get().setRevoked(true);
                    optionalRefreshToken.get().setExpired(true);
                    refreshTokenRepository.save(optionalRefreshToken.get());
                    SecurityContextHolder.clearContext();
                    return ResponseEntity.status(HttpStatus.OK)
                            .body(GenericResponse.builder()
                                    .success(true)
                                    .message("Đăng xuất thành công!")
                                    .result("")
                                    .statusCode(HttpStatus.OK.value())
                                    .build());
                }
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Đăng xuất thất bại!")
                                .result("")
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Đăng xuất thất bại!")
                            .result("")
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .build());

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> refreshAccessToken(String refreshToken) {
        try {
            String userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent() && optionalUser.get().isActive()) {
                //List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByUser_UserIdAndExpiredIsFalseAndRevokedIsFalse(userId);
                Optional<RefreshToken> token = refreshTokenRepository.findByUser_UserIdAndExpiredIsFalseAndRevokedIsFalse(userId);
                if (token.isPresent() && jwtTokenProvider.validateToken(token.get().getToken())) {
                    if (!token.get().getToken().equals(refreshToken)) {
                        return ResponseEntity.status(404)
                                .body(GenericResponse.builder()
                                        .success(false)
                                        .message("RefreshToken is not present. Làm ơn đăng nhập lại!")
                                        .result("")
                                        .statusCode(HttpStatus.NOT_FOUND.value())
                                        .build());
                    }
                    UserDetail userDetail = (UserDetail) userDetailsService.loadUserByUserId(jwtTokenProvider.getUserIdFromRefreshToken(refreshToken));
                    String accessToken = jwtTokenProvider.generateAccessToken(userDetail);
                    Map<String, String> resultMap = new HashMap<>();
                    resultMap.put("accessToken", accessToken);
                    resultMap.put("refreshToken", refreshToken);
                    return ResponseEntity.status(200)
                            .body(GenericResponse.builder()
                                    .success(true)
                                    .message("")
                                    .result(resultMap)
                                    .statusCode(HttpStatus.OK.value())
                                    .build());
                }
            }
            return ResponseEntity.status(401)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Không có quyền. Làm ơn đăng nhập lại!")
                            .result("")
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostConstruct
    public void init() {
        refreshTokenRepository.deleteAllByExpiredIsTrueAndRevokedIsTrue();
    }

    @Scheduled(fixedRate = 60000)
    public void cleanUpToken(){
        refreshTokenRepository.deleteAllByExpiredIsTrueAndRevokedIsTrue();
    }
}
