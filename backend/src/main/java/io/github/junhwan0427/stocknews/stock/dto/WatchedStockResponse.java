package io.github.junhwan0427.stocknews.stock.dto;

import io.github.junhwan0427.stocknews.stock.UserStock;
import java.time.Instant;

/**
 * 관심 종목 한 건의 응답 모양. Entity를 그대로 내보내지 않고 응답 전용 객체로 바꿔서 내보낸다.
 * (Entity를 그대로 내보내면 DB 구조가 API에 묶이고, 지연 로딩 필드 때문에 에러가 나기 쉽다)
 */
public record WatchedStockResponse(
        String ticker,
        String name,
        boolean notifyEnabled, // record 에선 notify 라는 이름을 못 쓴다 (모든 객체에 이미 notify() 메서드가 있어서)
        Instant addedAt
) {

    public static WatchedStockResponse from(UserStock userStock) {
        return new WatchedStockResponse(
                userStock.getStock().getTicker(),
                userStock.getStock().getName(),
                userStock.isNotify(),
                userStock.getCreatedAt()
        );
    }
}
