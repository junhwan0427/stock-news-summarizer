package io.github.junhwan0427.stocknews.common.exception;

import org.springframework.http.HttpStatus;

/** 관심 종목이 아닌 종목을 해제하려 할 때. */
public class NotWatchingException extends ApiException {

    public NotWatchingException(String ticker) {
        super(HttpStatus.NOT_FOUND, "관심 종목이 아님", "'" + ticker + "'은(는) 관심 종목이 아닙니다.");
    }
}
