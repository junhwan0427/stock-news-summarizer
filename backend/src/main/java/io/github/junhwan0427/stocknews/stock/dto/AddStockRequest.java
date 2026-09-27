package io.github.junhwan0427.stocknews.stock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * POST /me/stocks 요청 본문. 예: {"ticker": "AAPL"}
 * record: 필드, 생성자, getter, equals를 자동으로 만들어주는 Java 문법. 요청·응답 객체에 딱 맞다.
 */
public record AddStockRequest(

        @NotBlank(message = "티커를 입력하세요.")
        // 영문자로 시작, 영문자와 점(.)으로 최대 10자. 예: AAPL, BRK.B
        @Pattern(regexp = "^\\s*[A-Za-z][A-Za-z.]{0,9}\\s*$", message = "티커 형식이 올바르지 않습니다. 예: AAPL, BRK.B")
        String ticker
) {
}
