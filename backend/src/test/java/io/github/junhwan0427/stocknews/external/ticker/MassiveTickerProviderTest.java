package io.github.junhwan0427.stocknews.external.ticker;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.junhwan0427.stocknews.common.exception.ExternalApiException;
import io.github.junhwan0427.stocknews.external.massive.MassiveClientConfig;
import io.github.junhwan0427.stocknews.external.massive.MassiveProperties;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Massive 어댑터 테스트. WireMock 으로 가짜 Massive 서버를 띄우고, 미리 정한 응답을 돌려주게 한다.
 * 진짜 API를 안 부르니 빠르고, 키가 없어도 되고, 에러 상황도 마음대로 만들 수 있다. (설계서 B-6)
 */
class MassiveTickerProviderTest {

    @RegisterExtension
    static WireMockExtension massive = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort()) // 비어 있는 포트를 알아서 골라 띄운다
            .build();

    private MassiveTickerProvider provider;

    @BeforeEach
    void setUp() {
        MassiveProperties properties = new MassiveProperties(
                massive.baseUrl(), "test-key", Duration.ofSeconds(1), Duration.ofSeconds(1));
        provider = new MassiveTickerProvider(MassiveClientConfig.create(properties));
    }

    @Test
    void 상장_중인_미국_주식이면_티커와_회사명을_돌려주고_키는_헤더로_보낸다() {
        massive.stubFor(get(urlEqualTo("/v3/reference/tickers/AAPL")).willReturn(okJson("""
                {
                  "status": "OK",
                  "results": {
                    "ticker": "AAPL", "name": "Apple Inc.", "market": "stocks",
                    "locale": "us", "active": true, "type": "CS", "currency_name": "usd"
                  }
                }
                """)));

        Optional<TickerInfo> result = provider.findUsStock("AAPL");

        assertThat(result).contains(new TickerInfo("AAPL", "Apple Inc."));
        massive.verify(getRequestedFor(urlEqualTo("/v3/reference/tickers/AAPL")) // URL에 apiKey가 붙지 않았고
                .withHeader("Authorization", equalTo("Bearer test-key")));       // 헤더로 갔는지
    }

    @Test
    void 없는_티커라서_404면_빈_결과() {
        massive.stubFor(get(urlEqualTo("/v3/reference/tickers/APPLX")).willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                        {"status": "NOT_FOUND", "message": "Ticker not found."}
                        """)));

        assertThat(provider.findUsStock("APPLX")).isEmpty();
    }

    @Test
    void 미국_주식이_아니면_빈_결과() {
        massive.stubFor(get(urlEqualTo("/v3/reference/tickers/X:BTCUSD")).willReturn(okJson("""
                {"status": "OK", "results": {"ticker": "X:BTCUSD", "name": "Bitcoin", "market": "crypto",
                 "locale": "global", "active": true}}
                """)));

        assertThat(provider.findUsStock("X:BTCUSD")).isEmpty();
    }

    @Test
    void 상장폐지된_종목이면_빈_결과() {
        massive.stubFor(get(urlEqualTo("/v3/reference/tickers/OLD")).willReturn(okJson("""
                {"status": "OK", "results": {"ticker": "OLD", "name": "Old Corp", "market": "stocks",
                 "locale": "us", "active": false}}
                """)));

        assertThat(provider.findUsStock("OLD")).isEmpty();
    }

    @Test
    void 호출_제한_초과나_서버_오류면_ExternalApiException() {
        massive.stubFor(get(urlEqualTo("/v3/reference/tickers/AAPL")).willReturn(aResponse().withStatus(429)));

        assertThatThrownBy(() -> provider.findUsStock("AAPL"))
                .isInstanceOf(ExternalApiException.class);
    }

    @Test
    void 응답이_너무_늦으면_ExternalApiException() {
        massive.stubFor(get(urlEqualTo("/v3/reference/tickers/AAPL")).willReturn(okJson("{}")
                .withFixedDelay(2_000))); // 읽기 제한 1초보다 오래 걸리게

        assertThatThrownBy(() -> provider.findUsStock("AAPL"))
                .isInstanceOf(ExternalApiException.class);
    }
}
