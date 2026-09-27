package io.github.junhwan0427.stocknews.stock;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByTicker(String ticker);
}
