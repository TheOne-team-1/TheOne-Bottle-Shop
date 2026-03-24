-- chat_rooms 테이블 추가
CREATE TABLE chat_rooms
(
    id                            BIGINT AUTO_INCREMENT NOT NULL,
    created_at                    datetime     NOT NULL,
    updated_at                    datetime     NOT NULL,
    name                          VARCHAR(100) NOT NULL,
    customer_id                   BIGINT       NOT NULL,
    manager_id                    BIGINT       NULL,
    status                        VARCHAR(30)  NOT NULL,
    last_message_at               datetime     NULL,
    closed_at                     datetime     NULL,
    last_message_id               BIGINT       NULL,
    customer_last_read_message_id BIGINT       NULL,
    manager_last_read_mesasge_id  BIGINT       NULL,
    CONSTRAINT pk_chat_rooms PRIMARY KEY (id)
);

CREATE INDEX idx_chat_room_customer_id ON chat_rooms (customer_id);
CREATE INDEX idx_chat_room_manager_id ON chat_rooms (manager_id);
CREATE INDEX idx_chat_room_status_last_message_at ON chat_rooms (status, last_message_at);

-- chat_messages 테이블 추가
CREATE TABLE chat_messages
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    created_at   datetime      NOT NULL,
    updated_at   datetime      NOT NULL,
    chat_room_id BIGINT        NOT NULL,
    sender_id    BIGINT        NOT NULL,
    sender_type  VARCHAR(20)   NOT NULL,
    message_type VARCHAR(20)   NOT NULL,
    content      VARCHAR(1000) NOT NULL,
    deleted      BIT(1)        NOT NULL,
    CONSTRAINT pk_chat_messages PRIMARY KEY (id)
);

-- social_auth 테이블 추가 (V1 배포 이후 추가된 테이블)
CREATE TABLE social_auth
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    provider    VARCHAR(255)          NULL,
    provider_id VARCHAR(255)          NULL,
    member_id   BIGINT                NULL,
    CONSTRAINT pk_socialauth PRIMARY KEY (id)
);

CREATE INDEX idx_rating_created_at ON reviews (rating, created_at DESC);

CREATE INDEX idx_chat_message_room_id_id ON chat_messages (chat_room_id, id);
CREATE INDEX idx_chat_message_room_id_sender_type_id ON chat_messages (chat_room_id, sender_type, id);
CREATE INDEX idx_chat_message_sender_id ON chat_messages (sender_id);

-- member_address 누락 컬럼 추가 (@SQLDelete, @SQLRestriction 에서 참조)
ALTER TABLE member_address
    ADD COLUMN deleted    BIT(1)   NOT NULL DEFAULT FALSE,
    ADD COLUMN deleted_at datetime NULL;
