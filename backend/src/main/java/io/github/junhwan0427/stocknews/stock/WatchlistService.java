package io.github.junhwan0427.stocknews.stock;

import io.github.junhwan0427.stocknews.common.config.StockNewsProperties;
import io.github.junhwan0427.stocknews.common.exception.AlreadyWatchingException;
import io.github.junhwan0427.stocknews.common.exception.NotWatchingException;
import io.github.junhwan0427.stocknews.common.exception.TickerNotFoundException;
import io.github.junhwan0427.stocknews.common.exception.WatchlistLimitExceededException;
import io.github.junhwan0427.stocknews.external.ticker.TickerInfo;
import io.github.junhwan0427.stocknews.external.ticker.TickerProvider;
import io.github.junhwan0427.stocknews.stock.dto.WatchedStockResponse;
import io.github.junhwan0427.stocknews.user.CurrentUserProvider;
import io.github.junhwan0427.stocknews.user.User;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 관심 종목 추가·조회·해제 규칙. */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본은 읽기 전용. 쓰는 메서드에만 @Transactional 을 다시 붙인다.
public class WatchlistService {

    private final CurrentUserProvider currentUserProvider;
    private final StockRepository stockRepository;
    private final UserStockRepository userStockRepository;
    private final TickerProvider tickerProvider;
    private final StockNewsProperties properties;

    public List<WatchedStockResponse> getMyStocks() {
        User me = currentUserProvider.currentUser();
        return userStockRepository.findActiveWithStock(me.getId()).stream()
                .map(WatchedStockResponse::from)
                .toList();
    }

    @Transactional
    public WatchedStockResponse add(String rawTicker) {
        String ticker = normalize(rawTicker);
        User me = currentUserProvider.currentUser();

        // 1. 개수 제한 (관리자는 무제한)
        int max = properties.watchlist().maxPerUser();
        if (!me.isAdmin() && userStockRepository.countActive(me.getId()) >= max) {
            throw new WatchlistLimitExceededException(max);
        }

        // 2. 종목 찾기: DB에 있으면 그대로, 없으면 Massive로 검증 후 새로 저장
        Stock stock = stockRepository.findByTicker(ticker)
                .orElseGet(() -> registerNewStock(ticker));
        stock.activate();

        // 3. 관심 등록: 처음이면 새로 만들고, 해제했던 거면 되살리고, 이미 있으면 거절
        UserStock userStock = userStockRepository.findById(new UserStockId(me.getId(), stock.getId()))
                .map(existing -> {
                    if (!existing.isDeleted()) {
                        throw new AlreadyWatchingException(ticker);
                    }
                    existing.restore();
                    return existing;
                })
                .orElseGet(() -> userStockRepository.save(new UserStock(me, stock)));

        log.info("관심 종목 추가: userId={}, ticker={}", me.getId(), ticker);
        return WatchedStockResponse.from(userStock);
    }

    @Transactional
    public void remove(String rawTicker) {
        String ticker = normalize(rawTicker);
        User me = currentUserProvider.currentUser();

        UserStock userStock = stockRepository.findByTicker(ticker)
                .flatMap(stock -> userStockRepository.findById(new UserStockId(me.getId(), stock.getId())))
                .filter(us -> !us.isDeleted())
                .orElseThrow(() -> new NotWatchingException(ticker));

        userStock.delete(); // 변경 감지: 트랜잭션이 끝날 때 JPA가 UPDATE 문을 알아서 보낸다
        log.info("관심 종목 해제: userId={}, ticker={}", me.getId(), ticker);
    }

    private Stock registerNewStock(String ticker) {
        TickerInfo info = tickerProvider.findUsStock(ticker)
                .orElseThrow(() -> new TickerNotFoundException(ticker));
        log.info("새 종목 등록: ticker={}, name={}", info.ticker(), info.name());
        return stockRepository.save(new Stock(info.ticker(), info.name()));
    }

    /** " aapl " → "AAPL". 대소문자와 앞뒤 공백 차이로 같은 종목이 두 번 들어가지 않게. */
    private static String normalize(String rawTicker) {
        return rawTicker.trim().toUpperCase(Locale.ROOT);
    }
}
