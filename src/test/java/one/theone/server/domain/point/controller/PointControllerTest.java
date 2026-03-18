package one.theone.server.domain.point.controller;

import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.point.dto.*;
import one.theone.server.domain.point.entity.PointLog;
import one.theone.server.domain.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PointService pointService;

    @Test
    @DisplayName("포인트 조정 성공")
    @WithMockUser
    void adjustPoint_success() throws Exception {
        // given
        Long memberId = 1L;
        PointAdjustRequest request = new PointAdjustRequest(500L, "이벤트 보상");
        PointAdjustResponse response = new PointAdjustResponse(memberId, "이벤트 보상", 500L, 1500L);
        given(pointService.adjustPoint(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/admin/points/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("포인트 조정 성공"))
                .andExpect(jsonPath("$.data.memberId").value(1))
                .andExpect(jsonPath("$.data.amount").value(500))
                .andExpect(jsonPath("$.data.balance").value(1500));
    }

    @Test
    @DisplayName("포인트 내역 조회 성공")
    @WithMockUser
    void getPointLogs_success() throws Exception {
        // given
        PointLogsGetResponse item = new PointLogsGetResponse(
                1L, PointLog.PointType.EARN, "이벤트 보상",
                500L, 1500L, null,
                LocalDateTime.now(), LocalDate.now().plusDays(365)
        );
        PageResponse<PointLogsGetResponse> pageResponse = PageResponse.register(
                new PageImpl<>(List.of(item))
        );
        given(pointService.getPointLogs(any(), any(), any())).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/points")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("포인트 내역 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].type").value("EARN"))
                .andExpect(jsonPath("$.data.content[0].amount").value(500));
    }
}