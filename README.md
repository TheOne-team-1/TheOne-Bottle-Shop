# TheOne - 주류 바틀 샵 컨셉 백엔드 API 서버

---

## 프로젝트 소개

**TheOne**은 주류 바틀 샵 컨셉의 백엔드 API 서버
- 상품 검색 및 주문부터 결제, 포인트, 쿠폰, 사은품, 이벤트까지 쇼핑몰에 필요한 전반적인 기능을 제공
- 실시간 고객-관리자 채팅 기능을 WebSocket으로 제공
- AWS 인프라(EC2, RDS, ElastiCache, ALB)를 기반으로 운영되며, GitHub Actions를 통해 자동 배포

---

## 기술 스택

### Backend
- Java 21
- Spring Boot 4.0.3
- JPA, OpenFeign QueryDSL 7.1
- Spring Security, JWT, OAuth2
- Spring Cache, Redis, Caffeine
- Redis Pub/Sub
- Redisson 4.3
- Flyway
- 검색 형태소 분석 : Komorran
- WebSocket
- ULID, DataFaker

### Database & Infra
- MySQL 8.4.8
- AWS ElastiCache
- Docker, docker-compose
- GitHub Action

### AWS
- EC2
- RDS (MySQL 8.4.8)
- ElastiCache
- ALB

---

## 아키텍처

- ERD
<img width="3580" height="2842" alt="image" src="https://github.com/user-attachments/assets/8fd4b97f-e9a6-4bb3-9773-04b8ba2cd7d4" />

- AWS 구조
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/6b4f8f73-041c-4cdf-987f-f984933ad8a1" />

```
외부 인터넷
-> ALB -> EC2 -> RDS - ElastiCache
-> NAT Gateway
-> 외부 인터넷
```
---

### 도메인 패키지 구조

```
one.theone.server
├── common/              # 공통 설정 (Security, Redis, Exception, AOP 등)
└── domain/
    ├── auth/            # 인증 (JWT, OAuth2)
    ├── member/          # 회원, 주소
    ├── product/         # 상품, 최근 본 상품
    ├── category/        # 카테고리
    ├── cart/            # 장바구니
    ├── order/           # 주문
    ├── payment/         # 결제
    ├── refund/          # 환불
    ├── coupon/          # 쿠폰
    ├── freebie/         # 사은품
    ├── freebieCategory/ # 사은품 카테고리
    ├── event/           # 이벤트
    ├── point/           # 포인트
    ├── review/          # 리뷰
    ├── favorite/        # 즐겨찾기
    ├── search/          # 상품 검색 (KOMORAN)
    └── chat/            # 실시간 채팅 (WebSocket)
```

---

## 기능

### 회원
- 이메일/비밀번호 회원가입 및 로그인
- Google OAuth2 소셜 로그인
- JWT Access/Refresh Token 인증 (RTR)

### 상품
- 상품 목록 조회 (가격/평점/최신순 정렬, 카테고리 필터)
- 상품 상세 조회
- 최근 본 상품 조회 (Redis)

### 검색
- v1: 키워드 단순 매칭 검색
- v2: KOMORAN 형태소 분석 기반 검색
- 인기 검색어 랭킹 (Redis ZSet)
- 비회원 IP / 회원 ID 기반 중복 집계 처리

### 주문 및 결제
- 바로 구매 / 장바구니 구매
- Redisson 분산 락으로 재고 동시 감소 제어
- Facade 패턴으로 결제 플로우 원자적 처리 (재고 선점 → 쿠폰/사은품 예약 → 결제)
- 결제 확정 / 주문 취소

### 쿠폰 & 사은품 & 이벤트
- 쿠폰 발급 (이벤트 지급 / 직접 발급)
- 사은품 재고 관리
- 이벤트 생성 및 보상(쿠폰/사은품) 연계

### 포인트
- 결제 확정 시 포인트 자동 적립 (Redis Pub/Sub 비동기 처리)
- 주문 시 포인트 사용
- 환불 시 포인트 복원

### 리뷰
- 구매 확정 상품에 대한 리뷰 작성/수정/삭제
- 리뷰 좋아요 / 조회수

### 채팅
- WebSocket 기반 실시간 고객-관리자 1:1 채팅 기능
- 채팅방 생성, 관리자 배정, 상태 관리 (대기/진행/완료)
- 읽음 처리

### 환불
- 결제 건 환불 요청 및 관리자 처리

### 분산 락
- Redisson 을 통한 분산 락 (Pub/Sub)
- Lecttuce 를 통한 분산 락 (SpinLock)

---

## API

- `없음` -> 인증 불필요 / `JWT` = AccessToken 필요
- `ALL` -> 누구나 / `회원` -> 로그인 사용자 / `관리자` -> ROLE_ADMIN

### 인증

| Method | URI | 설명 | 인증 | 역할 |
|--------|-----|------|------|------|
| POST | `/api/signup` | 회원가입 | 없음 | ALL |
| POST | `/api/signup/admin` | 관리자 회원가입 | 없음 | ALL |
| POST | `/api/login` | 로그인 (Access/Refresh Token 발급) | 없음 | ALL |
| POST | `/api/reissue` | 토큰 재발급 | 없음 | ALL |
| GET | `/login/oauth2/**` | Google OAuth2 소셜 로그인 | 없음 | ALL |

### 회원

| Method | URI | 설명 | 인증 | 역할 |
|--------|-----|------|------|------|
| GET | `/api/me` | 내 정보 조회 | JWT | 회원 |
| PATCH | `/api/password` | 비밀번호 변경 | JWT | 회원 |

### 배송지

| Method | URI | 설명 | 인증 | 역할 |
|--------|-----|------|------|------|
| POST | `/api/addresses` | 배송지 등록 | JWT | 회원 |
| GET | `/api/addresses` | 배송지 목록 조회 | JWT | 회원 |
| PATCH | `/api/addresses/{addressId}` | 배송지 수정 | JWT | 회원 |
| DELETE | `/api/addresses/{addressId}` | 배송지 삭제 | JWT | 회원 |

### 상품

| Method | URI | 설명 | 인증 | 역할 |
|--------|-----|------|------|------|
| GET | `/api/products` | 상품 목록 조회 | 없음 | ALL |
| GET | `/api/products/{id}` | 상품 상세 조회 | 없음 | ALL |
| GET | `/api/best/products` | 베스트 상품 조회 | 없음 | ALL |
| GET | `/api/recently-viewed/products` | 최근 본 상품 조회 | JWT | 회원 |
| POST | `/api/admin/products` | 상품 등록 | JWT | 관리자 |
| GET | `/api/admin/products` | 관리자 상품 목록 조회 | JWT | 관리자 |
| PATCH | `/api/admin/products/{id}` | 상품 수정 | JWT | 관리자 |
| PATCH | `/api/admin/products/{id}/status` | 상품 상태 변경 | JWT | 관리자 |
| DELETE | `/api/admin/products/{id}` | 상품 삭제 | JWT | 관리자 |

### 카테고리

| Method | URI | 설명 | 인증 | 역할 |
|--------|-----|------|------|------|
| POST | `/api/admin/categories` | 대분류 카테고리 생성 | JWT | 관리자 |
| GET | `/api/admin/categories` | 카테고리 목록 조회 | JWT | 관리자 |
| PATCH | `/api/admin/categories/{id}` | 대분류 카테고리 수정 | JWT | 관리자 |
| DELETE | `/api/admin/categories/{id}` | 대분류 카테고리 삭제 | JWT | 관리자 |
| POST | `/api/admin/category-details` | 소분류 카테고리 생성 | JWT | 관리자 |
| PATCH | `/api/admin/category-details/{id}` | 소분류 카테고리 수정 | JWT | 관리자 |
| DELETE | `/api/admin/category-details/{id}` | 소분류 카테고리 삭제 | JWT | 관리자 |

### 장바구니

| Method | URI | 설명 | 인증 | 역할 |
|--------|-----|------|------|------|
| POST | `/api/carts/items` | 장바구니 상품 추가 | JWT | 회원 |
| GET | `/api/carts` | 장바구니 조회 | JWT | 회원 |
| PATCH | `/api/carts/items/{productId}` | 장바구니 수량 변경 | JWT | 회원 |
| DELETE | `/api/carts/items/{productId}` | 장바구니 상품 삭제 | JWT | 회원 |
| DELETE | `/api/carts` | 장바구니 전체 삭제 | JWT | 회원 |

### 주문

| Method | URI | 설명 | 인증 | 역할 |
|--------|-----|------|------|------|
| POST | `/api/orders/direct` | 바로 구매 주문 생성 | JWT | 회원 |
| POST | `/api/orders/cart` | 장바구니 주문 생성 | JWT | 회원 |
| GET | `/api/orders` | 주문 목록 조회 | JWT | 회원 |
| GET | `/api/orders/{orderId}` | 주문 상세 조회 | JWT | 회원 |
| PATCH | `/api/orders/{orderId}/cancel` | 주문 취소 | JWT | 회원 |

### 결제

| Method | URI | 설명 | 인증 | 역할 |
|--------|-----|------|------|------|
| POST | `/api/payments` | 결제 생성 | JWT | 회원 |
| PATCH | `/api/payments/{paymentId}/confirm` | 결제 확정 | JWT | 회원 |

### 환불

| Method | URI | 설명 | 인증 | 역할 |
|--------|-----|------|------|------|
| POST | `/api/refunds` | 환불 요청 | JWT | 회원 |
| POST | `/api/admin/refunds` | 관리자 환불 처리 | JWT | 관리자 |

### 쿠폰

| Method | URI | 설명 | 인증 | 역할 |
|--------|-----|------|------|------|
| GET | `/api/coupons/me` | 내 쿠폰 목록 조회 | JWT | 회원 |
| POST | `/api/coupons/{couponId}/issue` | 이벤트 쿠폰 발급 | JWT | 회원 |
| POST | `/api/admin/coupons` | 쿠폰 생성 | JWT | 관리자 |
| GET | `/api/admin/coupons` | 쿠폰 목록 조회 | JWT | 관리자 |
| GET | `/api/admin/coupons/{couponId}` | 쿠폰 상세 조회 | JWT | 관리자 |
| POST | `/api/admin/coupons/{couponId}/issue` | 관리자 쿠폰 발급 | JWT | 관리자 |
| PATCH | `/api/admin/coupons/{couponId}/expire` | 쿠폰 만료 처리 | JWT | 관리자 |
| PATCH | `/api/admin/member/{memberId}/coupons/{memberCouponId}/recall` | 쿠폰 회수 | JWT | 관리자 |

### 이벤트

| Method | URI | 설명 | 인증 | 역할 |
|--------|-----|------|------|------|
| GET | `/api/events` | 이벤트 목록 조회 | 없음 | ALL |
| GET | `/api/events/{eventId}` | 이벤트 상세 조회 | 없음 | ALL |
| POST | `/api/admin/events` | 이벤트 생성 | JWT | 관리자 |
| PATCH | `/api/admin/events/{eventId}/status` | 이벤트 상태 변경 | JWT | 관리자 |
| DELETE | `/api/admin/events/{eventId}` | 이벤트 삭제 | JWT | 관리자 |

### 사은품

| Method | URI | 설명 | 인증 | 역할 |
|--------|-----|------|------|------|
| POST | `/api/admin/freebies` | 사은품 생성 | JWT | 관리자 |
| GET | `/api/admin/freebies` | 사은품 목록 조회 | JWT | 관리자 |
| GET | `/api/admin/freebies/{id}` | 사은품 단건 조회 | JWT | 관리자 |
| PATCH | `/api/admin/freebies/{id}` | 사은품 수정 | JWT | 관리자 |
| DELETE | `/api/admin/freebies/{id}` | 사은품 삭제 | JWT | 관리자 |

### 사은품 카테고리

| Method | URI | 설명 | 인증 | 역할 |
|--------|-----|------|------|------|
| POST | `/api/admin/freebie-categories` | 사은품 카테고리 생성 | JWT | 관리자 |
| GET | `/api/admin/freebie-categories` | 사은품 카테고리 목록 조회 | JWT | 관리자 |
| PATCH | `/api/admin/freebie-categories/{id}` | 사은품 카테고리 수정 | JWT | 관리자 |
| DELETE | `/api/admin/freebie-categories/{id}` | 사은품 카테고리 삭제 | JWT | 관리자 |
| POST | `/api/admin/freebie-category-details` | 사은품 세부 카테고리 생성 | JWT | 관리자 |
| PATCH | `/api/admin/freebie-category-details/{id}` | 사은품 세부 카테고리 수정 | JWT | 관리자 |
| DELETE | `/api/admin/freebie-category-details/{id}` | 사은품 세부 카테고리 삭제 | JWT | 관리자 |

### 포인트

| Method | URI | 설명 | 인증 | 역할 |
|--------|-----|------|------|------|
| GET | `/api/points` | 포인트 내역 조회 | JWT | 회원 |
| POST | `/api/admin/points/{memberId}` | 포인트 수동 조정 | JWT | 관리자 |

### 리뷰

| Method | URI | 설명 | 인증 | 역할 |
|--------|-----|------|------|------|
| POST | `/api/review` | 리뷰 작성 | JWT | 회원 |
| GET | `/api/review` | 리뷰 목록 조회 | 없음 | ALL |
| GET | `/api/review/{id}` | 리뷰 상세 조회 | 없음 | ALL |
| POST | `/api/review/{id}/like` | 리뷰 좋아요 | JWT | 회원 |
| DELETE | `/api/review/{id}` | 리뷰 삭제 | JWT | 회원/관리자 |

### 검색

| Method | URI | 설명 | 인증 | 역할 |
|--------|-----|------|------|------|
| GET | `/api/search/v1` | 키워드 검색 (단순 매칭) | 없음 | ALL |
| GET | `/api/search/v2` | 키워드 검색 (형태소 분석) | 없음 | ALL |
| GET | `/api/best/search` | 인기 검색어 Top5 조회 | 없음 | ALL |

### 즐겨찾기

| Method | URI | 설명 | 인증 | 역할 |
|--------|-----|------|------|------|
| POST | `/api/favorites/{productId}` | 즐겨찾기 등록 | JWT | 회원 |
| DELETE | `/api/favorites/{productId}` | 즐겨찾기 삭제 | JWT | 회원 |
| GET | `/api/favorites` | 즐겨찾기 목록 조회 | JWT | 회원 |

### 채팅

| Method | URI | 설명 | 인증 | 역할 |
|--------|-----|------|------|------|
| POST | `/api/chat/rooms` | 채팅방 생성 | JWT | 회원 |
| GET | `/api/chat/rooms/me` | 내 채팅방 목록 조회 | JWT | 회원 |
| GET | `/api/chat/rooms` | 전체 채팅방 목록 조회 | JWT | 관리자 |
| GET | `/api/chat/rooms/{roomId}` | 채팅방 상세 조회 | JWT | 회원 |
| GET | `/api/chat/rooms/{roomId}/messages` | 메시지 목록 조회 (cursor 기반) | JWT | 회원 |
| PUT | `/api/chat/rooms/{roomId}/assign` | 관리자 배정 | JWT | 관리자 |
| PUT | `/api/chat/rooms/{roomId}/status` | 채팅방 상태 변경 | JWT | 관리자 |
| PATCH | `/api/chat/rooms/{roomId}/read` | 읽음 처리 | JWT | 회원 |
| PATCH | `/api/chat/rooms/{roomId}/messages/{messageId}/delete` | 메시지 삭제 | JWT | 회원 |

### STOMP

| 타입 | Destination | 설명 | 인증 | 역할 |
|------|-------------|------|------|------|
| PUBLISH | `/pub/chat/rooms/{roomId}` | 메시지 전송 | JWT (STOMP Header) | 회원/관리자 |
| SUBSCRIBE | `/sub/chat/rooms/{roomId}` | 메시지 수신 구독 | JWT (STOMP Header) | 회원/관리자 |

---

## 구현 쟁점

## 캐시
### 캐시 사용
- 검색 및 상품 목록, 이벤트 목록 등의 목록 조회 API의 경우 사용자가 대다수 같은 데이터를 조회
- 조회 전략은 Cache-Aside 전략 차용
  - Write-though 의 경우는 캐시와 DB 를 동시에 갱신 -> 실제로 조회되지 않는 데이터도 캐시 적재
  - Write-back 의 경우는 쓰기를 캐시에, 후에 DB 갱신 -> 서버 장애시 유실 위험, 재고의 정합성이 우려됨
  - **Cache-Aside** 의 경우는 캐시 Miss 시 DB 에서 값을 캐시에 저장 -> 실제로 조회된 데이터만 캐싱 처리, 변경시 무효화 가능
    - 또한 **@Cacheable 어노테이션이 기본 지원 하는 패턴** -> 더불어 Spring 이 직렬화/역직렬화 처리를 대신 함


### 캐시 키 및 TTL 현황

- Spring Cache의 기본 Redis 키 포맷은 `{cacheName}::{key}` 입니다.

- **Redis Cache (`redisCacheManager`, 기본 TTL 15분)**

| Cache Name | TTL | Key 패턴 | 적용 도메인 | 무효화 시점                   |
|---|---|---|---|--------------------------|
| `productCache` | 30분 | `list:{sortType}:{categoryIds}:{abvMin}:{abvMax}:{page}:{size}` | 상품 목록 조회 | 상품 생성/수정/삭제, 재고 변경 시     |
| `categoryCache` | 1시간 | `page:{page}:size:{size}` | 카테고리 목록 조회 | 카테고리 생성/수정/삭제 시          |
| `productSearch` | 30분 | `keyword:{keyword}:page:{n}:size:{n}` | 상품 검색 결과 | 상품 생성/수정/삭제 시            |
| `searchRanking` | 10분 | `ranking:top5` | 인기 검색어 Top5 | 검색어 집계 기록 시 (비동기 처리 구현)  |
| `eventListCache` | 5분 | `{isAdmin}:{status}:{page}:{size}` | 이벤트 목록 조회 | 이벤트 생성/수정/삭제, 재고 소진 시    |
| `reviewList` | 15분 | `{condition}:{pageNumber}` | 리뷰 목록 조회 | 리뷰 작성/수정/삭제 시            |
| `cartCache` | 15분 | `member:{memberId}` | 장바구니 조회 | 장바구니 추가/수량변경/삭제, 주문 생성 시 |
| `orderListCache` | 15분 | `member:{memberId}:page:{n}:size:{n}` | 주문 목록 조회 | 주문 생성/취소 시               |
| `orderDetailCache` | 1분 | `member:{memberId}:order:{orderId}` | 주문 상세 조회 | 주문 생성/취소 시               |

### 캐시 무효화 전략

#### **전체 무효화 (`allEntries = true`)**
- 적용: `productCache`, `categoryCache`, `productSearch`, `eventListCache`, `reviewList`, `orderListCache`, `orderDetailCache`
- 이유: 정렬·필터·페이지 조합이 키에 포함되어 있어, 특정 항목 하나만 골라 삭제하는 것이 불가능
- 트레이드오프: 쓰기가 잦을수록 캐시 히트율이 낮아질 수 있음


#### **키 지정 무효화 (`key = "'member:' + #memberId"`)**
- 적용: `cartCache`
- 이유: 장바구니는 회원별로 완전히 독립된 데이터, 다른 회원 캐시에 영향을 줄 이유가 없음
- 트레이드오프: 키 패턴이 조회·무효화 코드 양쪽에서 동일하게 유지되어야 함

#### **조건부 무효화 (`condition = "#result == true"`)**
- 적용: `productCache`, `productSearch` (재고 감소 시)
- 이유: 재고 차감이 실제로 성공한 경우에만 캐시를 삭제
- 트레이드오프: 불필요한 캐시 무효화를 줄일 수 있지만, 조건의 반환값과 자료형 차이가 발생시 오류

#### **TTL 기반 자동 만료**
- 모든 캐시에 TTL을 설정해 무효화가 실패해도 일정 시간이 지나면 만료되도록 함


### 캐시 전 후 성능 비교
- 성능비교 대상
  - 검색어 기능 V1 (캐시 미사용), V2 (캐시 사용)

- 검색어 기능 캐시 관리 흐름
  - 인기 검색어 기능은 **ZSet** 위에 **Spring Cache** 를 적용한 구조

  | Redis 키 패턴 | 자료구조 | TTL | 역할                                |
  |---|---|---|-----------------------------------|
  | `search:dedup:{keyword}:{identifier}` | String | 30분 | 동일 사용자의 같은 키워드 중복 집계 방지           |
  | `search:ranking:week:{year}:{week}` | ZSet | 8일 | 주간 키워드별 스코어                       |
  | `searchRanking::ranking:top5` | String (JSON) | 10분 | ZSet 조회 결과 Top5 를 Spring Cache로 캐싱 |

  **검색 시 흐름 (집계 단계)**
  ```
  사용자 검색
  -> SearchRankingService.record()
  -> SETNX search:dedup:{keyword}:{identifier} (TTL 30분)
      -> 이미 존재시, 스코어 반영 안 함
      -> 신규 검색시 ZINCRBY search:ranking:week:{year}:{week} 1 {keyword}
         -> @CacheEvict(searchRanking, allEntries=true)
            -> searchRanking::ranking:top5 삭제
  ```

  **인기 검색어 조회 흐름**
  ```
  GET /api/best/search
  -> @Cacheable(searchRanking, key="ranking:top5")
     -> 캐시 HIT  → searchRanking::ranking:top5 반환 (DB·ZSet 미조회)
     -> 캐시 MISS → ZREVRANGE search:ranking:week:{year}:{week} 0 4
        -> Top5 결과를 searchRanking::ranking:top5 에 저장 (TTL 10분)
  ```

- 성능 테스트 비교 (K6)

- V1
<img width="1870" height="1384" alt="image" src="https://github.com/user-attachments/assets/b4ec7014-d62a-4c23-98d8-ec0bdf57eed8" />

  
- V2
<img width="1924" height="1370" alt="image" src="https://github.com/user-attachments/assets/fc9e5f61-c5d2-43d5-a1d5-efef340bb6ce" />


  | 지표 | V1 (캐시 적용 X) | V2 (캐시 적용 O) |
  |------|-----------------|-----------------|
  | 평균 응답 시간 | 3.74s | 2.56ms |
  | TPS | 70.8/s | 2,611/s |
  | 에러율 | 0% | 0% |
  | 총 요청 수 | 17,007 | 626,805 |
  | p90 | 7.08s | 4.01ms |
  | p95 | 7.19s | 6.08ms |
  | 500ms 초과 비율 | 89% | 0% |

### 로컬 캐시의 한계
- 로컬 캐시 (Caffeine) 의 경우엔 Scale-Out 으로 서버 확장시 공유할 수 없음
- 그러므로 더 Redis를 선택


## 동시성 제어

### 동시성 문제
- 주문 시 상품에 다수의 재고 차감이 동시에 진행될 경우
- 이벤트 사은품이 동시에 여러 건이 지급되는 경우
- 한정된 수량의 쿠폰에 지급이 동시에 이루어 질 경우
  - 테스트 코드로 증빙 (대표 예시 -> test - product - service 내 다수)

### 최종적으로 선택하여 사용한 Lock
- 낙관적 락 만으로는 충돌 시 재시도 로직이 복잡해지고 데이터 정합성을 보장하기 힘듬
- 분산 락 의 경우 다중 서버 (Scale-Out) 로 추후 구성되지 않는 한 비관적 락에 비해 구현도가 높아 부적절
  - 다만 다중 서버로 구성 될 여지는 항상 있으니 분산 락을 기반으로 다른 락을 추가하는 등으로 구현
> 현 상황에서 대부분 사용되어야 할 Lock 은 비관적 락이나 구현은 분산 락과 비관적 락이 혼재됨

### 분산 락 구현점
#### 실패 전략
- 즉시 실패, 재시도 전략 둘 다 수용할 수 있도록 구성 -> 결제 관련 재고 차감 처리의 경우는 즉시 실패로, 증가는 재시도로

#### 공통 구현부
- 메서드 레벨, 런타임 시 동작하도록 지정하기 위한 커스텀 어노테이션
  - @RedisLock, @RedissonLock
- 별도의 REQUIRES_NEW 전파를 통한 트랜잭션 구성으로 비즈니스 로직을 락 트랜잭션이 감싸는 구조로 처리
  - 락 해제 전 비즈니스 로직의 트랜잭션이 커밋되도록 보장
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisLock {
    String key();
    long waitTime() default 5L;
    long leaseTime() default 10L;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
```

```java
@Component
public class AopInTransaction {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object proceed(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
```

#### Redis Lettuce 를 통한 SpinLock
- waitTime 이 0 지정 시 즉시 실패 전략, 0 이상시 100ms 마다 재시도 전략을 취할 수 있도록 구성
- watchDog 을 통해 자동 TTL 증가 전략 -> 작업이 길어지면 락 만료를 통한 동시 처리 위험성 방지
- UUID 를 통한 개별 락 값 -> 다른 서버의 락을 해제할 수 없도록 보장
- Lua Script 를 통한 원자성 부여 -> 조회-삭제 작업을 하나로 묶어 작업 사이에 다른 작업이 끼어들지 않도록 보장
- finally 구문 -> 예외 발생에도 락 점유 방지

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockService {
  private static final long INTERVAL_WAIT_TIME = 100;
  private static final long WATCH_DOG_INCREMENT_TIME = 1000;

  private final RedisLockRepository redisLockRepository;

  private final ScheduledExecutorService watchDogExecutor = Executors.newScheduledThreadPool(8);

  // 락 시도
  public String tryLock(String key, long waitTime, long leaseTime, TimeUnit unit)
          throws InterruptedException {
    long deadline = System.currentTimeMillis() + unit.toMillis(waitTime);
    while (System.currentTimeMillis() < deadline) {
      String lockValue = redisLockRepository.getLock(key, leaseTime, unit);
      if (lockValue != null) return lockValue;
      Thread.sleep(INTERVAL_WAIT_TIME);
    }
    return null;
  }

  // 락 해제
  public void unLock(String key, String lockValue) {
    redisLockRepository.checkOwnLock(key, lockValue);
  }

  // WatchDog
  public ScheduledFuture<?> setWatchDog(String key, String lockValue, long leaseTime, TimeUnit unit) {
    return watchDogExecutor.scheduleAtFixedRate(() -> redisLockRepository.setWatchDog(key, lockValue, leaseTime, unit)
            , WATCH_DOG_INCREMENT_TIME, WATCH_DOG_INCREMENT_TIME, TimeUnit.MILLISECONDS
    );
  }
}
```
```java
@Component
@RequiredArgsConstructor
public class RedisLockRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    // 락 획득하기
    public String getLock(String key, long leaseTime, TimeUnit unit) {
        String lockValue = UUID.randomUUID().toString();// 개별 LockValue 를 위한 UUID 셋업
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, lockValue, leaseTime, unit);
        return Boolean.TRUE.equals(result) ? lockValue : null;
    }

    // 원자성 체크 (자기 락 확인)
    public void checkOwnLock(String key, String lockValue) {
        String luaScript =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "    return redis.call('del', KEYS[1]) " +
                        "else return 0 end";
        redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                Collections.singletonList(key), lockValue
        );
    }

    // WatchDog - 원자성 체크 후 아직 내 락이면 TTL 자동 연장
    public void setWatchDog(String key, String lockValue, long leaseTime, TimeUnit unit) {
        String luaScript =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "    return redis.call('expire', KEYS[1], ARGV[2]) " +
                        "else return 0 end";
        redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                Collections.singletonList(key),
                lockValue, String.valueOf(unit.toSeconds(leaseTime))
        );
    }
}
```

#### Redisson
- 라이브러리 내 
  - 재시도 전략과 즉시 실패 전략 (waitTime 이 0이면 즉시 실패 전략)
  - 작업이 길어질 경우 락 만료시 처리 방안 (WatchDog) (leaseTime 이 -1이면 WatchDog 활성화)
  - 락 해제시 자신이 건 락인지 확인
  - 락 해제가 원자적인지
    - 이 모두가 작동 되도록 되어 있음
- 설정부에 Cold Start 방지를 위해 아이들 상태 커넥션은 2개 유지
```java
@Configuration
public class RedissonConfig {
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setConnectionMinimumIdleSize(2)
                .setConnectionPoolSize(10);
        return Redisson.create(config);
    }
}

```
### 재고 감소 동시성 처리 테스트
- 100개의 상품재고가 100개의 스레드 동시 차감 진행
- 사진

## 인덱싱
- index 설정 대상 대표 쿼리
  - **상품 목록 조회** - 고객이 대다수 이용하는 API 이며, 정렬 수행에 따라 자주 변경될 수 있는 대표적 쿼리
    - ORDER BY, LIMIT 등 데이터가 쌓일 수록 성능이 낮아짐

```SQL
SELECT id, name, price, status, rating
FROM products
WHERE deleted = false
  AND status IN ('SALES', 'SOLD_OUT')
ORDER BY created_at DESC
LIMIT 10 OFFSET 0;
```

<img width="1077" height="94" alt="image" src="https://github.com/user-attachments/assets/b61d9909-18ab-41b0-90e9-5eaebd960d3e" />
<img width="1033" height="87" alt="image" src="https://github.com/user-attachments/assets/d5df9d25-15f2-4fab-bd2f-65e687af6556" />
- 전체 테이블 풀 스캔 발생

#### WHERE 절 사항들로 인덱스 생성
  - deleted, status
<img width="1074" height="93" alt="image" src="https://github.com/user-attachments/assets/b23c01b5-8499-4b84-837b-86a8310a59c4" />
실패, 카디널리티가 낮아 옵티마이저가 인덱스 사용 판단을 하지 않은 듯, 여전히 풀스캔

#### WHERE 절 사항 + ORDER BY 절 사항 1
  - deleted, status, created_at
<img width="1075" height="91" alt="image" src="https://github.com/user-attachments/assets/3f462888-79f4-41f2-b16e-f1c6743e9f2b" />
실패, 인덱스 내부적으로 status 값들이 created_at에 의해 정렬 -> 합쳤을 때 정렬 불가
실패, 카디널리티 낮은 문제도 여전히 발생 -> 여전히 풀스캔

#### WHERE 절 사항 + ORDER BY 절 사항 2
  - created_at, deleted, status
<img width="1432" height="92" alt="image" src="https://github.com/user-attachments/assets/018266ad-3d91-4adc-8c18-4d5cb187c722" />
<img width="1062" height="88" alt="image" src="https://github.com/user-attachments/assets/99ce661a-03d7-4535-8978-12f69eae8b8d" />
성공, created_at 을 선두로 정렬, deleted가 false 인 조건대로 status 가 SALES, SOLD_OUT 인 10건을 가져오면 됨 

```SQL
CREATE INDEX idx_product_created_at_deleted_status ON products (created_at, deleted, status);
```

## 실시간 채팅
#### 관리자-고객 간 상담을 고려한 실시간 채팅 구성
- WebSocket 기반 실시간 통신 구현
  - STOMP 사용으로 메세지를 보낼 목적지, 받을 구독, 특정 목적지를 향한 대상 구성
    - `/sub/chat/room` 를 통해 특정 채팅방을 구독하고 메세지를 전송할 수 있도록 구현
  - 커서 기반 페이징 처리
    - 메세지 목록에 대해 커서 기반 페이징 처리를 구현하여 사용자의 이전 대화 내용 호출에 용이하도록 조치
      - lastMessageId 보다 작은 아이디를 가져와 활용
    ```java
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
    ```
#### 사용자 인증 처리
- WebSocket 의 지속된 연결에 따라 유효성을 인증할 방법을 고려해야함
- 누가 보냈는가에 대한 확인도 필요함
  - StompAuthChannelInterceptor 를 통해 인증 정보를 계속 활용할 수 있도록 기능 구현
```
ChatMessageController 의 Authentication
-> StompAuthChannelInterceptor 에서 메세지 도달 전 preSend 를 통해 헤더에 Authorization 에서 토큰 탈취
  -> 토큰 검증
-> 유효시 UsernamePasswordAuthenticationToken 인증 정보를 생성
-> StompHeaderAccessor 에 저장
-> Spring Messaging 이 자동으로 인증 객체를 스레드에 복사
-> ChatMessageController 에서 메세지 호출 시점에 꺼내서 인증 객체 파라미터에 자동 주입
-> Authentication 활용 가능
```

## CI/CD
#### CI/CD 흐름
``` 
Dev Push/PR 
-> 빌드 및 테스트 
-> 빌드 결과물 업로드

Main Push
Dev Push/PR 흐름
-> 빌드 결과물 다운로드
-> dockerFile 이미지 배포
-> EC2 환경 에서 이미지 pull
-> 헬스 체크
```
#### SSM 사용
- SSH 사용시 22번 포트를 추가 인바운드 설정 해야함
  - 포트 개방 없이 aws CLI 를 통해 접근이 가능
  - Bastion 을 통한 관문 서버 관리 필요 없음
- IAM Policy 로 관리 가능

#### GitHub Action
- Git Hub 사용시 설정의 쉬운 이점으로 사용
- 민감 정보도 Secrets 로 관리
