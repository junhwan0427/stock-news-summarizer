package io.github.junhwan0427.stocknews.external.massive;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Massive API를 부를 RestClient를 하나 만들어 스프링에 등록한다.
 * 종목 조회, 뉴스, 일봉, 휴장일 어댑터가 모두 이 클라이언트를 같이 쓴다.
 */
@Configuration
public class MassiveClientConfig {

    public static final String BEAN_NAME = "massiveRestClient";

    @Bean(BEAN_NAME)
    RestClient massiveRestClient(MassiveProperties properties) {
        return create(properties);
    }

    /** 테스트에서도 같은 설정으로 만들 수 있게 분리했다. */
    public static RestClient create(MassiveProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout()); // 서버에 연결될 때까지 최대 대기
        requestFactory.setReadTimeout(properties.readTimeout());       // 응답이 올 때까지 최대 대기

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                // 키를 URL(?apiKey=...)이 아니라 헤더로 보낸다. URL은 로그·에러 메시지에 찍히기 쉬워서. (설계서 B-5)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .requestFactory(requestFactory)
                .build();
    }
}
