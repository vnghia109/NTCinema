package vn.iostar.NT_cinema.exception;

import com.google.firebase.messaging.FirebaseMessagingException;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import vn.iostar.NT_cinema.dto.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.AccessDeniedException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<ObjectError> details = ex.getBindingResult().getAllErrors();
        Map<String, String> errors = new HashMap<>();
        details.forEach((error) ->{
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("Xác thực không thành công!")
                .result(errors)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();
        return new ResponseEntity<>(genericResponse ,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .result(null)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FirebaseMessagingException.class)
    public ResponseEntity<?> handleFirebaseMessagingException(FirebaseMessagingException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("Gửi thông báo đẩy thất bại."+ex.getMessage())
                .result(null)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("Người dùng không tồn tại!")
                .result(ex.getMessage())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(RuntimeException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("Sai tên người dùng!")
                .result(ex.getMessage())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(RuntimeException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("Không tìm thấy")
                .result(ex.getMessage())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<?> handleAlreadyExistException(RuntimeException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("Đã tồn tại!")
                .result(ex.getMessage())
                .statusCode(HttpStatus.CONFLICT.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabledException(DisabledException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("Tài khoản của bạn chưa xác minh hoặc đã bị xóa!")
                .result(ex.getMessage())
                .statusCode(HttpStatus.FORBIDDEN.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("Tài Khoản không tồn tại hoặc sai mật khẩu!")
                .result(ex.getMessage())
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("Invalid token. Làm ơn đăng nhập lại!")
                .result(ex.getMessage())
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("The file size exceeds the maximum upload limit.")
                .result(ex.getMessage())
                .statusCode(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .build();
         return new ResponseEntity<>(genericResponse,HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleIOException(IOException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("Lỗi máy chủ")
                .result(ex.getMessage())
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({ RuntimeException.class })
    public ResponseEntity<Object> handleInternal(RuntimeException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("Lỗi máy chủ. "+ex.getMessage())
                .result("InternalError")
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<Object> handleBadRequest(Exception ex) {
        GenericResponse genericResponse = new GenericResponse(
                false,
                ex.getMessage(),
                "Bad Request",
                HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(genericResponse);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Object> handleExpiredJwtException(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                GenericResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .result("làm ơn đăng nhập lại!")
                        .statusCode(HttpStatus.UNAUTHORIZED.value()).build()
        );
    }

    @ExceptionHandler(ParseException.class)
    public ResponseEntity<Object> handleParseException(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                GenericResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .result("Không đúng định dạng!")
                        .statusCode(HttpStatus.BAD_REQUEST.value()).build()
        );
    }

    @ExceptionHandler(UnsupportedEncodingException.class)
    public ResponseEntity<Object> handleUnsupportedEncodingException(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                GenericResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .result("Unsupported Encoding!")
                        .statusCode(HttpStatus.BAD_REQUEST.value()).build()
        );
    }
}
