package one.theone.server.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.domain.auth.dto.LoginRequest;
import one.theone.server.domain.auth.dto.TokenResponse;
import one.theone.server.domain.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

     //로그인 API - 성공 시 Access/Refresh Token 발급
     //실패 시 Redis에 기록하여 3회 실패 시 30초 차단
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("로그인 시작: {}", request.email());
            TokenResponse response = authService.login(request);
            log.info("로그인 성공! 토큰: {}", response.accessToken());
            return ResponseEntity.ok(BaseResponse.success("OK", "성공", response));
        } catch (Exception e) {
            log.error("에러내용 : ", e); // 에러 내용 다 찍어라
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.fail("ERROR", e.getMessage()));
        }
    }
}
