package io.github.junhwan0427.stocknews.external.ticker;

import java.util.Optional;

/**
 * 포트: "이 티커가 상장 중인 미국 주식인지, 회사명이 뭔지" 알려주는 것. (설계서 B-3, 헥사고날의 포트)
 * 서비스 코드는 이 인터페이스만 알고, Massive를 쓰는지는 모른다.
 */
public interface TickerProvider {

    /** 상장 중인 미국 주식이면 정보를, 아니면(없는 티커, 다른 나라, 상장폐지) 빈 값을 돌려준다. */
    Optional<TickerInfo> findUsStock(String ticker);
}
