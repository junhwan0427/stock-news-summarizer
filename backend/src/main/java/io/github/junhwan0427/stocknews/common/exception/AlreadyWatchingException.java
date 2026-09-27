package io.github.junhwan0427.stocknews.common.exception;

import org.springframework.http.HttpStatus;

/** 이미 관심 종목으로 등록된 종목을 또 추가할 때. */
public class AlreadyWatchingException extends ApiException {

    public AlreadyWatchingException(String ticker) {
        super(HttpStatus.CONFLICT, "이미 관심 종목", "'" + ticker + "'은(는) 이미 관심 종목입니다.");
    }
}
