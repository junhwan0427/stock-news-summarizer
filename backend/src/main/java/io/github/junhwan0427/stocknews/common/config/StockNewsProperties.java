package io.github.junhwan0427.stocknews.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml 의 stocknews.* 설정을 타입이 있는 객체로 받는다.
 * 문자열 키로 여기저기서 @Value를 쓰는 것보다 오타가 컴파일 단계에서 잡힌다.
 */
@ConfigurationProperties("stocknews")
public record StockNewsProperties(FixedUser fixedUser, Watchlist watchlist) {

    /** 인증 전까지 모든 요청을 처리할 고정 사용자. */
    public record FixedUser(String email) {
    }

    /** 관심 종목 제한. */
    public record Watchlist(int maxPerUser) {
    }
}
