package io.github.junhwan0427.stocknews.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 우리 코드가 의도적으로 던지는 예외의 부모.
 * 전역 핸들러(GlobalExceptionHandler)가 status/title/detail 을 RFC 7807 응답으로 바꾼다. (설계서 B-4)
 */
@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String title;

    protected ApiException(HttpStatus status, String title, String detail) {
        super(detail);
        this.status = status;
        this.title = title;
    }

    protected ApiException(HttpStatus status, String title, String detail, Throwable cause) {
        super(detail, cause);
        this.status = status;
        this.title = title;
    }
}
