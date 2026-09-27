package io.github.junhwan0427.stocknews;

import org.springframework.boot.SpringApplication;

public class TestStockNewsApplication {

	public static void main(String[] args) {
		SpringApplication.from(StockNewsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
