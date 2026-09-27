package io.github.junhwan0427.stocknews.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 모든 컨트롤러에서 튀어나온 예외를 한 곳에서 받아 RFC 7807 형식으로 바꾸는 "고객센터". (설계서 B-4)
 * ResponseEntityExceptionHandler 를 상속해서 스프링 기본 예외(검증 실패, 잘못된 JSON 등)도 같은 형식으로 나간다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ProblemDetail handleApiException(ApiException e) {
        if (e.getStatus().is5xxServerError()) {
            log.error("{}: {}", e.getTitle(), e.getMessage(), e);
        } else {
            log.debug("{}: {}", e.getTitle(), e.getMessage());
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(e.getStatus(), e.getMessage());
        problem.setTitle(e.getTitle());
        return problem;
    }

    /**
     * @Valid 검증 실패. 기본 응답은 "Invalid request content." 뿐이라 뭐가 틀렸는지 모른다.
     * 필드별 메시지를 detail 과 errors 에 넣어준다. 예: {"errors": {"ticker": "티커 형식이 올바르지 않습니다."}}
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = ex.getBody();
        problem.setTitle("입력값 오류");
        problem.setDetail(String.join(" ", errors.values()));
        problem.setProperty("errors", errors);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }
}
