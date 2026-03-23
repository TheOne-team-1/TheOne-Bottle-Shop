package one.theone.server.domain.chat.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import one.theone.server.domain.chat.entity.ChatMessage;
import one.theone.server.domain.chat.entity.QChatMessage;
import one.theone.server.domain.chat.entity.SenderType;

import java.util.List;

import static one.theone.server.domain.chat.entity.QChatMessage.chatMessage;

@RequiredArgsConstructor
public class ChatMessageRepositoryImpl implements ChatMessageRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatMessage> findMessages(Long roomId, Long lastMessageId, int size) {
        return queryFactory
                .selectFrom(chatMessage)
                .where(
                        chatMessage.chatRoomId.eq(roomId),
                        ltMessageId(lastMessageId)
                )
                .orderBy(chatMessage.id.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression ltMessageId(Long lastMessageId) {
        return lastMessageId != null ? chatMessage.id.lt(lastMessageId) : null;
    }

    @Override
    public long countUnread(Long roomId, Long lastReadMessageId, SenderType senderType) {
        QChatMessage message = QChatMessage.chatMessage;

        Long count = queryFactory
                .select(message.count())
                .from(message)
                .where(
                        message.chatRoomId.eq(roomId),
                        lastReadMessageId != null ? message.id.gt(lastReadMessageId) : null,
                        message.senderType.eq(senderType),
                        message.deleted.isFalse()
                )
                .fetchOne();
        return count == null ? 0L : count;
    }
}
