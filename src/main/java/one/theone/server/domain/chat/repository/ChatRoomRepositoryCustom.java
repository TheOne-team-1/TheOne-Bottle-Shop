package one.theone.server.domain.chat.repository;

import one.theone.server.domain.chat.entity.ChatRoom;
import one.theone.server.domain.chat.entity.ChatRoomStatus;

import java.util.List;

public interface ChatRoomRepositoryCustom {
    List<ChatRoom> findMyRooms(Long customerId);
    List<ChatRoom> findAdminRooms(ChatRoomStatus status);
}
