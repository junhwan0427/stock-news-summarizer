-- 사용자. 인증 컬럼(비밀번호 해시, 카카오 ID 등)과 알림 컬럼은 해당 기능을 만들 때 추가한다.
CREATE TABLE users (
    id          BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    nickname    VARCHAR(50)  NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMPTZ
);
