package one.theone.server.domain.chat.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import one.theone.server.domain.chat.entity.ChatRoom;
import one.theone.server.domain.chat.entity.ChatRoomStatus;

import java.util.List;

import static one.theone.server.domain.chat.entity.QChatRoom.chatRoom;

@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatRoom> findMyRooms(Long customerId) {
        return queryFactory
                .selectFrom(chatRoom)
                .where(chatRoom.customerId.eq(customerId))
                .orderBy(
                        chatRoom.lastMessageAt.desc(),
                        chatRoom.createdAt.desc()
                )
                .fetch();
    }

    @Override
    public List<ChatRoom> findAdminRooms(ChatRoomStatus status) {
        return queryFactory
                .selectFrom(chatRoom)
                .where(
                        statusEq(status)
                )
                .orderBy(
                        chatRoom.lastMessageAt.desc(),
                        chatRoom.createdAt.desc()
                )
                .fetch();
    }

    private com.querydsl.core.types.dsl.BooleanExpression statusEq(ChatRoomStatus status) {
        return status != null ? chatRoom.status.eq(status) : null;
    }
}
