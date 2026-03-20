package one.theone.server.domain.member.controller;

import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.MemberExceptionEnum;
import one.theone.server.domain.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    @WithMockUser
    void join_Success() throws Exception {
        String content = """
                {
                    "email": "test@test.com",
                    "password": "password123",
                    "passwordConfirm": "password123",
                    "name": "전민우",
                    "birthAt": "19950101",
                    "address": "서울시",
                    "addressDetail": "101호",
                    "privacyPolicyAgreed": true
                }
                """;

        mockMvc.perform(post("/api/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("내 정보 조회 성공 테스트")
    @WithMockUser(username = "1")
    void getMyInfo_Success() throws Exception {
        mockMvc.perform(get("/api/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("관리자 회원가입 성공 테스트")
    @WithMockUser(roles = "ADMIN")
    void joinAdmin_Success() throws Exception {
        String content = """
            {
                "email": "admin@test.com",
                "password": "password123",
                "name": "관리자",
                "adminCode": "SECRET_CODE" 
            }
            """;

        mockMvc.perform(post("/api/signup/admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("비밀번호 변경 성공 테스트")
    @WithMockUser(username = "1")
    void changePassword_Success() throws Exception {
        String content = """
            {
                "currentPassword": "password123",
                "newPassword": "newPassword123!"
            }
            """;

        mockMvc.perform(patch("/api/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("비밀번호 변경 실패 - 잘못된 JSON 형식 (400)")
    @WithMockUser(username = "1")
    void changePassword_Fail_InvalidJson() throws Exception {
        String brokenJson = "{ \"currentPassword\": \"1234\", ";

        mockMvc.perform(patch("/api/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenJson))
                .andExpect(status().isBadRequest());
    }
    @Test
    @DisplayName("내 정보 조회 실패 - 서비스 에러 발생 (404)")
    @WithMockUser(username = "1")
    void getMyInfo_Fail_ServiceError() throws Exception {
        given(memberService.getMyInfo(anyLong()))
                .willThrow(new ServiceErrorException(MemberExceptionEnum.ERR_MEMBER_NOT_FOUND));

        mockMvc.perform(get("/api/me"))
                .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("인증 없이 접근 시 401 반환")
    void access_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    @DisplayName("회원가입 실패 - 유효하지 않은 이메일")
    @WithMockUser
    void join_Fail_InvalidEmail() throws Exception {
        // 기존에 필드들이 누락되어 400 에러가 났던 데이터를 수정합니다.
        String invalidContent = """
            {
                "email": "not-an-email", 
                "password": "password123!",
                "passwordConfirm": "password123!",
                "name": "전민우",
                "birthAt": "1995-01-01",
                "address": "서울시 중랑구",
                "privacyPolicyAgreed": true
            }
            """;

        mockMvc.perform(post("/api/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidContent))
                .andExpect(status().isBadRequest());
    }
}