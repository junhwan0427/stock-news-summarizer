-- 시스템이 아는 종목. 사용자 수와 무관하게 티커당 한 행.
-- active: 시스템이 매일 수집·요약할지. 관심 사용자가 0명이 되면 채점 작업이 false로 끈다.
CREATE TABLE stocks (
    id          BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticker      VARCHAR(10)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMPTZ
);
