package one.theone.server.domain.auth.controller;

import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.MemberExceptionEnum;
import one.theone.server.domain.auth.dto.LoginRequest;
import one.theone.server.domain.auth.dto.TokenResponse;
import one.theone.server.domain.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    @WithMockUser
    @DisplayName("로그인 성공: 200 OK와 토큰을 반환한다")
    void login_Success() throws Exception {
        // given
        TokenResponse tokenResponse = new TokenResponse("mock_access_token", "mock_refresh_token");

        // authService.login이 호출될 때 위 tokenResponse를 반환하도록 명시
        given(authService.login(any(LoginRequest.class))).willReturn(tokenResponse);

        String content = """
            {
                "email": "test@test.com",
                "password": "password123"
            }
            """;

        // when & then
        mockMvc.perform(post("/api/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.data.accessToken").value("mock_access_token"))
                .andExpect(jsonPath("$.data.refreshToken").value("mock_refresh_token"));
    }

    @Test
    @WithMockUser
    @DisplayName("로그인 실패: 잘못된 비밀번호 입력 시 500 에러와 ERROR 상태를 반환한다")
    void login_Fail_InternalError() throws Exception {
        // given
        given(authService.login(any(LoginRequest.class)))
                .willThrow(new ServiceErrorException(MemberExceptionEnum.ERR_INVALID_PASSWORD));

        String content = """
            {
                "email": "test@test.com",
                "password": "wrong"
            }
            """;

        // when & then
        mockMvc.perform(post("/api/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 일치하지 않습니다"));
    }

    @Test
    @WithMockUser
    @DisplayName("토큰 재발급 성공: status 200과 새로운 토큰을 반환한다")
    void reissue_Success() throws Exception {
        // given
        TokenResponse response = new TokenResponse("new_access_token", "new_refresh_token");
        given(authService.reissue(anyString(), anyLong())).willReturn(response);

        String content = """
            {
                "refreshToken": "old_refresh_token",
                "memberId": 1
            }
            """;

        // when & then
        mockMvc.perform(post("/api/reissue")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.message").value("토큰 재발급 완료"))
                .andExpect(jsonPath("$.data.accessToken").value("new_access_token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new_refresh_token"));
    }
}