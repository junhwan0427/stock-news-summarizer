-- 사용자별 관심 종목. 해제는 소프트 삭제(deleted_at), 다시 추가하면 같은 행을 되살린다.
CREATE TABLE user_stocks (
    user_id     BIGINT      NOT NULL REFERENCES users (id),
    stock_id    BIGINT      NOT NULL REFERENCES stocks (id),
    notify      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMPTZ,
    PRIMARY KEY (user_id, stock_id)
);

-- 종목별 활성 사용자 수 조회용 (미관심 종목 비활성화, 알림 대상 조회). 삭제 안 된 행만 색인한다.
CREATE INDEX idx_user_stocks_stock_active ON user_stocks (stock_id) WHERE deleted_at IS NULL;
