package one.theone.server.domain.member.controller;

import jakarta.persistence.EntityNotFoundException;
import one.theone.server.domain.member.dto.MemberAddressRequest;
import one.theone.server.domain.member.dto.MemberAddressResponse;
import one.theone.server.domain.member.service.MemberAddressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(MemberAddressController.class)
@AutoConfigureMockMvc
class MemberAddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberAddressService memberAddressService;

    private final String validJson = """
{
    "addressName": "집",
    "address": "서울시 중랑구...",
    "addressDetail": "101호",
    "zipCode": "12345",
    "defaultYn": false
}
""";

    @Test
    @DisplayName("신규 배송지 등록 API 성공 (201 Created)")
    @WithMockUser(username = "1")
    void registerAddress_Success() throws Exception {
        MemberAddressResponse response = new MemberAddressResponse(1L, 1L, "집", "서울시...", false);

        given(memberAddressService.registerAddress(eq(1L), any(MemberAddressRequest.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/addresses")
                        .with(csrf())
                        .content(validJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("배송지 등록 성공"));
    }


    @Test
    @DisplayName("특정 배송지 정보 수정 API 성공")
    @WithMockUser(username = "1")
    void updateAddress_Success() throws Exception {
        MemberAddressResponse response = new MemberAddressResponse(1L, 1L, "수정된 집", "서울시...", false);

        given(memberAddressService.updateAddress(anyLong(), anyLong(), any(MemberAddressRequest.class)))
                .willReturn(response);

        mockMvc.perform(patch("/api/addresses/1")
                        .with(csrf())
                        .content(validJson) // 수정된 JSON 사용
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    @DisplayName("배송지 삭제 API 성공")
    @WithMockUser(username = "1")
    void deleteAddress_Success() throws Exception {
        mockMvc.perform(delete("/api/addresses/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("인증 정보가 올바르지 않을 때 IllegalStateException 발생 시뮬레이션")
    @WithAnonymousUser
    void getAuthenticatedMemberId_Invalid() throws Exception {
        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    @DisplayName("존재하지 않는 배송지 수정 시 404 반환")
    @WithMockUser(username = "1")
    void updateAddress_NotFound() throws Exception {
        // given
        given(memberAddressService.updateAddress(anyLong(), anyLong(), any(MemberAddressRequest.class)))
                .willThrow(new EntityNotFoundException("배송지를 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(patch("/api/addresses/999")
                        .with(csrf())
                        .content(validJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("권한이 없는 배송지 삭제 시 403 반환")
    @WithMockUser(username = "1")
    void deleteAddress_Forbidden() throws Exception {
        doThrow(new AccessDeniedException("권한이 없습니다."))
                .when(memberAddressService).deleteAddress(anyLong(), anyLong());

        mockMvc.perform(delete("/api/addresses/2")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
    @Test
    @DisplayName("필수 값이 누락된 요청 시 400 반환")
    @WithMockUser(username = "1")
    void registerAddress_ValidationFailed() throws Exception {
        String invalidJson = """
    {
        "addressName": "", 
        "address": "",
        "defaultYn": false 
    }
    """;

        mockMvc.perform(post("/api/addresses")
                        .with(csrf())
                        .content(invalidJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    @Test
    @DisplayName("배송지 목록 조회 성공")
    @WithMockUser(username = "1")
    void getAddresses_Success() throws Exception {
        // given
        List<MemberAddressResponse> responses = List.of(
                new MemberAddressResponse(1L, 1L, "집", "주소", true)
        );
        given(memberAddressService.getMemberAddresses(anyLong())).willReturn(responses);

        // when & then
        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}