package io.github.junhwan0427.stocknews.external.massive;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** application.yml 의 massive.* 설정. */
@ConfigurationProperties("massive")
public record MassiveProperties(
        String baseUrl,
        String apiKey,
        Duration connectTimeout,
        Duration readTimeout
) {
}
