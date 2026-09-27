package io.github.junhwan0427.stocknews.external.ticker;

import io.github.junhwan0427.stocknews.common.exception.ExternalApiException;
import io.github.junhwan0427.stocknews.external.massive.MassiveClientConfig;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 어댑터: TickerProvider 포트를 Massive API로 구현한다.
 * GET /v3/reference/tickers/{ticker} → 종목 상세.
 */
@Component
public class MassiveTickerProvider implements TickerProvider {

    private final RestClient restClient;

    public MassiveTickerProvider(@Qualifier(MassiveClientConfig.BEAN_NAME) RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Optional<TickerInfo> findUsStock(String ticker) {
        try {
            TickerDetailsResponse response = restClient.get()
                    .uri("/v3/reference/tickers/{ticker}", ticker)
                    .retrieve()
                    .body(TickerDetailsResponse.class);

            if (response == null || response.results() == null) {
                return Optional.empty();
            }
            TickerDetailsResponse.Result result = response.results();
            boolean isActiveUsStock = "stocks".equals(result.market())
                    && "us".equals(result.locale())
                    && Boolean.TRUE.equals(result.active());
            if (!isActiveUsStock) {
                return Optional.empty();
            }
            return Optional.of(new TickerInfo(result.ticker(), result.name()));

        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty(); // 없는 티커: 정상적인 "없음" 결과
        } catch (RestClientException e) {
            // 키 오류(401), 호출 제한 초과(429), 서버 오류(5xx), 타임아웃 등: 우리가 고칠 수 없는 실패
            throw new ExternalApiException("Massive", "종목 조회에 실패했습니다: " + ticker, e);
        }
    }

    /** Massive 응답 중 우리가 쓰는 필드만. 나머지 필드는 무시된다. */
    record TickerDetailsResponse(String status, Result results) {

        record Result(String ticker, String name, String market, String locale, Boolean active) {
        }
    }
}
