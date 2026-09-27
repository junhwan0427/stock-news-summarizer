package io.github.junhwan0427.stocknews.common.exception;

import org.springframework.http.HttpStatus;

/** 외부 API(Massive, Claude 등)가 실패했을 때. 우리 잘못이 아니므로 502 Bad Gateway. */
public class ExternalApiException extends ApiException {

    public ExternalApiException(String service, String detail, Throwable cause) {
        super(HttpStatus.BAD_GATEWAY, service + " API 오류", detail, cause);
    }
}
