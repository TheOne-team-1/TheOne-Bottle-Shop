CREATE TABLE categories
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime     NOT NULL,
    updated_at datetime     NOT NULL,
    name       VARCHAR(100) NOT NULL,
    sort_num   INT NULL,
    deleted    BIT(1) NULL,
    deleted_at datetime NULL,
    CONSTRAINT pk_categories PRIMARY KEY (id)
);

CREATE TABLE category_details
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    created_at  datetime     NOT NULL,
    updated_at  datetime     NOT NULL,
    category_id BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    sort_num    INT NULL,
    deleted     BIT(1) NULL,
    deleted_at  datetime NULL,
    CONSTRAINT pk_category_details PRIMARY KEY (id)
);

CREATE TABLE coupons
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    created_at      datetime     NOT NULL,
    updated_at      datetime     NOT NULL,
    name            VARCHAR(255) NOT NULL,
    use_type        VARCHAR(255) NOT NULL,
    min_price       BIGINT       NOT NULL,
    discount_value  BIGINT       NOT NULL,
    avail_quantity  BIGINT       NOT NULL,
    issued_quantity BIGINT       NOT NULL,
    start_at        datetime     NOT NULL,
    end_at          datetime NULL,
    deleted         BIT(1)       NOT NULL,
    deleted_at      datetime NULL,
    CONSTRAINT pk_coupons PRIMARY KEY (id)
);

CREATE TABLE event_details
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    created_at       datetime NOT NULL,
    updated_at       datetime NOT NULL,
    event_id         BIGINT   NOT NULL,
    min_price        BIGINT NULL,
    event_product_id BIGINT NULL,
    deleted          BIT(1)   NOT NULL,
    deleted_at       datetime NULL,
    CONSTRAINT pk_event_details PRIMARY KEY (id)
);

CREATE TABLE event_logs
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    created_at      datetime     NOT NULL,
    updated_at      datetime     NOT NULL,
    event_id        BIGINT       NOT NULL,
    event_reward_id BIGINT       NOT NULL,
    member_id       BIGINT       NOT NULL,
    order_id        BIGINT NULL,
    event_at        datetime NULL,
    status          VARCHAR(255) NOT NULL,
    CONSTRAINT pk_event_logs PRIMARY KEY (id)
);

CREATE TABLE event_rewards
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    created_at  datetime     NOT NULL,
    updated_at  datetime     NOT NULL,
    event_id    BIGINT       NOT NULL,
    reward_type VARCHAR(255) NOT NULL,
    coupon_id   BIGINT NULL,
    freebie_id  BIGINT NULL,
    deleted     BIT(1)       NOT NULL,
    deleted_at  datetime NULL,
    CONSTRAINT pk_event_rewards PRIMARY KEY (id)
);

CREATE TABLE events
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime     NOT NULL,
    updated_at datetime     NOT NULL,
    name       VARCHAR(255) NOT NULL,
    start_at   datetime     NOT NULL,
    end_at     datetime     NOT NULL,
    type       VARCHAR(255) NOT NULL,
    status     VARCHAR(255) NOT NULL,
    deleted    BIT(1)       NOT NULL,
    deleted_at datetime NULL,
    CONSTRAINT pk_events PRIMARY KEY (id)
);

CREATE TABLE favorites
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NOT NULL,
    updated_at datetime NOT NULL,
    member_id  BIGINT   NOT NULL,
    product_id BIGINT   NOT NULL,
    CONSTRAINT pk_favorites PRIMARY KEY (id)
);

CREATE TABLE freebie_category_details
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    created_at          datetime NOT NULL,
    updated_at          datetime NOT NULL,
    freebie_category_id BIGINT NULL,
    name                VARCHAR(100) NULL,
    sort_num            INT NULL,
    deleted             BIT(1)   NOT NULL,
    deleted_at          datetime NULL,
    CONSTRAINT pk_freebie_category_details PRIMARY KEY (id)
);

CREATE TABLE freebie_categorys
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NOT NULL,
    updated_at datetime NOT NULL,
    name       VARCHAR(100) NULL,
    sort_num   INT NULL,
    deleted    BIT(1)   NOT NULL,
    deleted_at datetime NULL,
    CONSTRAINT pk_freebie_categorys PRIMARY KEY (id)
);

CREATE TABLE freebies
(
    id                         BIGINT AUTO_INCREMENT NOT NULL,
    created_at                 datetime     NOT NULL,
    updated_at                 datetime     NOT NULL,
    freebie_category_detail_id BIGINT       NOT NULL,
    name                       VARCHAR(255) NOT NULL,
    quantity                   BIGINT       NOT NULL,
    status                     VARCHAR(255) NOT NULL,
    deleted                    BIT(1)       NOT NULL,
    deleted_at                 datetime NULL,
    CONSTRAINT pk_freebies PRIMARY KEY (id)
);

CREATE TABLE member_address
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    created_at     datetime     NOT NULL,
    updated_at     datetime     NOT NULL,
    member_id      BIGINT       NOT NULL,
    address        VARCHAR(500) NOT NULL,
    address_detail VARCHAR(500) NOT NULL,
    default_yn     BIT(1)       NOT NULL,
    CONSTRAINT pk_member_address PRIMARY KEY (id)
);

CREATE TABLE member_coupons
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime     NOT NULL,
    updated_at datetime     NOT NULL,
    member_id  BIGINT       NOT NULL,
    coupon_id  BIGINT       NOT NULL,
    event_id   BIGINT NULL,
    issue_way  VARCHAR(255) NOT NULL,
    status     VARCHAR(255) NOT NULL,
    deleted    BIT(1)       NOT NULL,
    deleted_at datetime NULL,
    CONSTRAINT pk_member_coupons PRIMARY KEY (id)
);

CREATE TABLE member_recommend_logs
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    created_at       datetime NOT NULL,
    updated_at       datetime NOT NULL,
    vote_member_id   BIGINT   NOT NULL,
    target_member_id BIGINT   NOT NULL,
    recommend_at     datetime NOT NULL,
    CONSTRAINT pk_member_recommend_logs PRIMARY KEY (id)
);

CREATE TABLE members
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    created_at       datetime     NOT NULL,
    updated_at       datetime     NOT NULL,
    email            VARCHAR(255) NOT NULL,
    passwd           VARCHAR(255) NOT NULL,
    name             VARCHAR(255) NOT NULL,
    birth_at         VARCHAR(8)   NOT NULL,
    `role`           VARCHAR(255) NOT NULL,
    recommend_code   VARCHAR(255) NOT NULL,
    grade            VARCHAR(255) NOT NULL,
    grade_at         datetime     NOT NULL,
    total_pay_amount BIGINT       NOT NULL,
    deleted          BIT(1)       NOT NULL,
    deleted_at       datetime NULL,
    CONSTRAINT pk_members PRIMARY KEY (id)
);

CREATE TABLE order_details
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    created_at         datetime     NOT NULL,
    updated_at         datetime     NOT NULL,
    order_id           BIGINT       NOT NULL,
    product_id         BIGINT       NOT NULL,
    product_name_snap  VARCHAR(255) NOT NULL,
    product_price_snap BIGINT       NOT NULL,
    quantity           INT          NOT NULL,
    line_amount        BIGINT       NOT NULL,
    CONSTRAINT pk_order_details PRIMARY KEY (id)
);

CREATE TABLE orders
(
    id                         BIGINT AUTO_INCREMENT NOT NULL,
    created_at                 datetime     NOT NULL,
    updated_at                 datetime     NOT NULL,
    member_id                  BIGINT       NOT NULL,
    member_coupon_id           BIGINT NULL,
    order_num                  VARCHAR(20)  NOT NULL,
    status                     VARCHAR(255) NOT NULL,
    used_point                 BIGINT       NOT NULL,
    total_amount               BIGINT       NOT NULL,
    discount_amount            BIGINT       NOT NULL,
    final_amount               BIGINT       NOT NULL,
    member_address_snap        VARCHAR(500) NOT NULL,
    member_address_detail_snap VARCHAR(500) NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

CREATE TABLE payments
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    created_at        datetime     NOT NULL,
    updated_at        datetime     NOT NULL,
    order_id          BIGINT       NOT NULL,
    payment_unique_id VARCHAR(255) NOT NULL,
    status            VARCHAR(255) NOT NULL,
    price_snap        BIGINT       NOT NULL,
    pay_at            datetime NULL,
    deleted           BIT(1)       NOT NULL,
    deleted_at        datetime NULL,
    CONSTRAINT pk_payments PRIMARY KEY (id)
);

CREATE TABLE point_logs
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    created_at       datetime     NOT NULL,
    updated_at       datetime     NOT NULL,
    member_id        BIGINT       NOT NULL,
    order_id         BIGINT NULL,
    type             VARCHAR(255) NOT NULL,
    `description`    VARCHAR(255) NULL,
    amount           BIGINT NULL,
    remaining_amount BIGINT NULL,
    balance_snap     BIGINT NULL,
    expires_at       date NULL,
    CONSTRAINT pk_point_logs PRIMARY KEY (id)
);

CREATE TABLE point_use_details
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    created_at   datetime NOT NULL,
    updated_at   datetime NOT NULL,
    point_log_id BIGINT   NOT NULL,
    order_id     BIGINT   NOT NULL,
    amount       BIGINT   NOT NULL,
    refunded     BIT(1) NULL,
    CONSTRAINT pk_point_use_details PRIMARY KEY (id)
);

CREATE TABLE points
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NOT NULL,
    updated_at datetime NOT NULL,
    member_id  BIGINT   NOT NULL,
    balance    BIGINT NULL,
    deleted    BIT(1) NULL,
    deleted_at datetime NULL,
    version    BIGINT NULL,
    CONSTRAINT pk_points PRIMARY KEY (id)
);

CREATE TABLE products
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    created_at         datetime      NOT NULL,
    updated_at         datetime      NOT NULL,
    name               VARCHAR(255)  NOT NULL,
    price              BIGINT        NOT NULL,
    status             VARCHAR(255)  NOT NULL,
    abv                DECIMAL(5, 3) NOT NULL,
    volume_ml          INT           NOT NULL,
    category_detail_id BIGINT        NOT NULL,
    quantity           BIGINT        NOT NULL,
    rating             DECIMAL(2, 1) NULL,
    deleted            BIT(1) NULL,
    deleted_at         datetime NULL,
    version            BIGINT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id)
);

CREATE TABLE refunds
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    created_at         datetime     NOT NULL,
    updated_at         datetime     NOT NULL,
    order_id           BIGINT       NOT NULL,
    payment_id         BIGINT       NOT NULL,
    price              BIGINT       NOT NULL,
    status             VARCHAR(255) NOT NULL,
    reason             VARCHAR(255) NOT NULL,
    reason_description VARCHAR(255) NULL,
    refund_at          datetime NULL,
    deleted            BIT(1)       NOT NULL,
    deleted_at         datetime NULL,
    CONSTRAINT pk_refunds PRIMARY KEY (id)
);

CREATE TABLE reviews
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    created_at      datetime      NOT NULL,
    updated_at      datetime      NOT NULL,
    order_detail_id BIGINT        NOT NULL,
    member_id       BIGINT        NOT NULL,
    product_id      BIGINT        NOT NULL,
    rating          INT           NOT NULL,
    title           VARCHAR(100)  NOT NULL,
    content         VARCHAR(2000) NOT NULL,
    like_count      INT           NOT NULL,
    view_count      INT           NOT NULL,
    deleted         BIT(1)        NOT NULL,
    deleted_at      datetime NULL,
    CONSTRAINT pk_reviews PRIMARY KEY (id)
);

ALTER TABLE points
    ADD CONSTRAINT uc_6a156dd9234b48df83212251e UNIQUE (member_id);

ALTER TABLE categories
    ADD CONSTRAINT uc_8c269303ee284e8482d8b499d UNIQUE (name);

ALTER TABLE members
    ADD CONSTRAINT uc_members_email UNIQUE (email);

ALTER TABLE members
    ADD CONSTRAINT uc_members_recommendcode UNIQUE (recommend_code);

ALTER TABLE orders
    ADD CONSTRAINT uc_orders_order_num UNIQUE (order_num);

ALTER TABLE reviews
    ADD CONSTRAINT uc_reviews_orderdetailid UNIQUE (order_detail_id);

ALTER TABLE category_details
    ADD CONSTRAINT uk_category_details_category_id_name UNIQUE (category_id, name);

ALTER TABLE favorites
    ADD CONSTRAINT uk_favorites_member_product UNIQUE (member_id, product_id);

ALTER TABLE payments
    ADD CONSTRAINT uk_payment_unique_id UNIQUE (payment_unique_id);

CREATE INDEX idx_favorite_member_id_created_at ON favorites (member_id, created_at);

CREATE INDEX idx_order_details_order_id ON order_details (order_id);

CREATE INDEX idx_orders_member_created_at ON orders (member_id, created_at);

CREATE INDEX idx_point_log_member_id_created_at ON point_logs (member_id, created_at);

CREATE INDEX idx_product_created_at_deleted_status ON products (created_at, deleted, status);

CREATE INDEX idx_product_price_deleted_status ON products (price, deleted, status);

CREATE INDEX idx_product_rating_deleted_status ON products (rating, deleted, status);

