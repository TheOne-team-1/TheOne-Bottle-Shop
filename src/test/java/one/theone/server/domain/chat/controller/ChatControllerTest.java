package one.theone.server.domain.chat.controller;

import one.theone.server.domain.chat.dto.request.ChatRoomCreateRequest;
import one.theone.server.domain.chat.dto.request.ChatRoomStatusUpdateRequest;
import one.theone.server.domain.chat.entity.ChatRoomStatus;
import one.theone.server.domain.chat.service.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatService chatService;

    @Test
    @DisplayName("채팅방 생성 성공")
    @WithMockUser
    void createRoom_success() throws Exception {
        // given
        ChatRoomCreateRequest request = new ChatRoomCreateRequest("배송 문의");

        // when & then
        mockMvc.perform(post("/api/chat/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated());

        then(chatService).should().createRoom(any(), any(ChatRoomCreateRequest.class));
    }

    @Test
    @DisplayName("내 채팅방 목록 조회 성공")
    @WithMockUser
    void getMyRooms_success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/chat/rooms/me")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        then(chatService).should().getMyRooms(any());
    }

    @Test
    @DisplayName("관리자 전체 채팅방 목록 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getAdminRooms_success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/chat/rooms")
                        .param("status", "WAITING")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        then(chatService).should().getAdminRooms(any(), eq(ChatRoomStatus.WAITING));
    }

    @Test
    @DisplayName("채팅방 상세 조회 성공")
    @WithMockUser
    void getRoom_success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/chat/rooms/{roomId}", 1L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        then(chatService).should().getRoom(any(), eq(1L));
    }

    @Test
    @DisplayName("채팅 메시지 조회 성공")
    @WithMockUser
    void getMessages_success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/chat/rooms/{roomId}/messages", 1L)
                        .param("lastMessageId", "10")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        then(chatService).should().getMessages(any(), eq(1L), eq(10L));
    }

    @Test
    @DisplayName("채팅방 상태 변경 성공")
    @WithMockUser(roles = "ADMIN")
    void updateStatus_success() throws Exception {
        // given
        ChatRoomStatusUpdateRequest request = new ChatRoomStatusUpdateRequest(ChatRoomStatus.COMPLETED);

        // when & then
        mockMvc.perform(put("/api/chat/rooms/{roomId}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        then(chatService).should().updateStatus(any(), eq(1L), any(ChatRoomStatusUpdateRequest.class));
    }

    @Test
    @DisplayName("관리자 배정 성공")
    @WithMockUser(roles = "ADMIN")
    void assignManager_success() throws Exception {
        // when & then
        mockMvc.perform(put("/api/chat/rooms/{roomId}/assign", 1L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        then(chatService).should().assignManager(any(), eq(1L));
    }

    @Test
    @DisplayName("채팅방 읽음 처리 성공")
    @WithMockUser
    void markAsRead_success() throws Exception {
        // when & then
        mockMvc.perform(patch("/api/chat/rooms/{roomId}/read", 1L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        then(chatService).should().markAsRead(any(), eq(1L));
    }

    @Test
    @DisplayName("채팅 메시지 삭제 성공")
    @WithMockUser
    void deleteMessage_success() throws Exception {
        // when & then
        mockMvc.perform(patch("/api/chat/rooms/{roomId}/messages/{messageId}/delete", 1L, 10L)                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        then(chatService).should().deleteMessage(any(), eq(1L), eq(10L));
    }
}