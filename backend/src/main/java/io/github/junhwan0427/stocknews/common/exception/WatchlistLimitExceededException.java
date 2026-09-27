package io.github.junhwan0427.stocknews.common.exception;

import org.springframework.http.HttpStatus;

/** 일반 사용자가 관심 종목 최대 개수를 넘겨 추가하려 할 때. Claude 비용 통제용 제한. */
public class WatchlistLimitExceededException extends ApiException {

    public WatchlistLimitExceededException(int max) {
        super(HttpStatus.CONFLICT, "관심 종목 수 초과",
                "관심 종목은 최대 " + max + "개까지 등록할 수 있습니다.");
    }
}
