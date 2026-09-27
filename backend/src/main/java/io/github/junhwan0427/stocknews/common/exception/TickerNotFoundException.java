package io.github.junhwan0427.stocknews.common.exception;

import org.springframework.http.HttpStatus;

/** 입력한 티커가 상장 중인 미국 주식으로 확인되지 않을 때. */
public class TickerNotFoundException extends ApiException {

    public TickerNotFoundException(String ticker) {
        super(HttpStatus.BAD_REQUEST, "존재하지 않는 종목",
                "'" + ticker + "'은(는) 상장 중인 미국 주식으로 확인되지 않습니다.");
    }
}
