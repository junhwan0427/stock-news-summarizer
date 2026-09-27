package io.github.junhwan0427.stocknews.external.ticker;

/** 종목 조회 결과. 외부 API가 뭐든 우리 코드는 이 모양만 안다. */
public record TickerInfo(String ticker, String name) {
}
