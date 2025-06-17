package com.ptpt.authservice.exception.handler;

import com.ptpt.authservice.controller.response.CustomApiResponse;
import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exception.AuthServiceException;
import com.ptpt.authservice.exception.social.SocialPlatformException;
import com.ptpt.authservice.exception.social.SocialTokenInvalidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//@RestControllerAdvice(annotations = {RestController.class}, basePackages = {"com.ptpt.authservice.controller"})
//출처: https://dev-meung.tistory.com/entry/해커톤-HY-THON-트러블슈팅-Swagger-500-에러-Failed-to-load-API-definition [IT::Coding:티스토리]
//@RestControllerAdvice(annotations = {RestController.class}, basePackageClasses = {
//        AuthController.class,
//        SocialController.class,
//        UserController.class
//})
@RestControllerAdvice(basePackages = {"com.ptpt.authservice.controller"})
public class GlobalExceptionHandler {

    // 커스텀 Auth 예외 처리 (Enum 기반)
    @ExceptionHandler(AuthServiceException.class)
    public ResponseEntity<CustomApiResponse<Void>> handleAuthServiceException(AuthServiceException ex) {
        CustomApiResponse<Void> response = CustomApiResponse.of(ex.getResponseCode(), null);

        // 커스텀 메시지가 있는 경우 사용
        if (!ex.getMessage().equals(ex.getResponseCode().getDefaultMessage())) {
            response = CustomApiResponse.<Void>builder()
                    .success(false)
                    .code(ex.getResponseCode().getCode())
                    .message(ex.getMessage())
                    .data(null)
                    .build();
        }

        HttpStatus status = determineHttpStatusFromCode(ex.getResponseCode().getCode());
        return new ResponseEntity<>(response, status);
    }

    // Spring Security 예외 처리 (컨트롤러 레벨에서 발생하는 경우)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CustomApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        ApiResponseCode responseCode = determineAuthResponseCode(ex);
        CustomApiResponse<Void> response = CustomApiResponse.of(responseCode, null);

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({SocialTokenInvalidException.class, SocialPlatformException.class})
    public ResponseEntity<CustomApiResponse<Void>> handleSocialException(AuthServiceException ex) {
        CustomApiResponse<Void> response = CustomApiResponse.of(ex.getResponseCode(), null);

        // 커스텀 메시지가 있는 경우 사용
        if (!ex.getMessage().equals(ex.getResponseCode().getDefaultMessage())) {
            response = CustomApiResponse.<Void>builder()
                    .success(false)
                    .code(ex.getResponseCode().getCode())
                    .message(ex.getMessage())
                    .data(null)
                    .build();
        }

        HttpStatus status = ex.getResponseCode().getCode().equals("E0113") ?
                HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(response, status);
    }

    // 일반적인 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomApiResponse<Void>> handleException(Exception ex) {
        CustomApiResponse<Void> response = CustomApiResponse.<Void>builder()
                .success(false)
                .code("E9999")
                .message("서버 내부 오류가 발생했습니다.")
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ApiResponseCode determineAuthResponseCode(AuthenticationException ex) {
        return switch (ex.getClass().getSimpleName()) {
            case "BadCredentialsException" -> ApiResponseCode.AUTH_LOGIN_FAILED;
            case "InsufficientAuthenticationException" -> ApiResponseCode.AUTH_UNAUTHORIZED;
            case "DisabledException", "AccountExpiredException",
                 "CredentialsExpiredException", "LockedException" -> ApiResponseCode.AUTH_LOGIN_FAILED;
            default -> ApiResponseCode.AUTH_LOGIN_FAILED;
        };
    }

    private HttpStatus determineHttpStatusFromCode(String errorCode) {
        if (errorCode.startsWith("E01") || errorCode.startsWith("E04")) { // 인증 관련
            return HttpStatus.UNAUTHORIZED;
        } else if (errorCode.equals("E0409")) { // 권한 관련
            return HttpStatus.FORBIDDEN;
        } else if (errorCode.startsWith("E02")) { // 사용자 관련
            if (errorCode.contains("READ")) {
                return HttpStatus.NOT_FOUND;
            } else if (errorCode.contains("CREATE") && errorCode.contains("ALREADY")) {
                return HttpStatus.CONFLICT;
            }
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.BAD_REQUEST;
    }
}