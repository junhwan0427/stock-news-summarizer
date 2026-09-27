package io.github.junhwan0427.stocknews;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan // @ConfigurationProperties 가 붙은 설정 record들을 찾아서 등록
public class StockNewsApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockNewsApplication.class, args);
	}

}
