package io.github.junhwan0427.stocknews.stock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 관심 종목 규칙 단위 테스트. DB·외부 API 없이 서비스 로직만 확인한다. (설계서 B-6)
 * Mockito 의 mock(): 진짜 대신 끼워 넣는 가짜 객체. given(...).willReturn(...) 으로 "이렇게 물으면 이렇게 답해"를 정한다.
 */
class WatchlistServiceTest {

    private static final int MAX = 5;

    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private final StockRepository stockRepository = mock(StockRepository.class);
    private final UserStockRepository userStockRepository = mock(UserStockRepository.class);
    private final TickerProvider tickerProvider = mock(TickerProvider.class);

    private WatchlistService service;
    private User user;

    @BeforeEach
    void setUp() {
        StockNewsProperties properties = new StockNewsProperties(
                new StockNewsProperties.FixedUser("test@local"),
                new StockNewsProperties.Watchlist(MAX));
        service = new WatchlistService(currentUserProvider, stockRepository, userStockRepository,
                tickerProvider, properties);

        user = userWith(1L, false);
        given(currentUserProvider.currentUser()).willReturn(user);
        given(userStockRepository.save(any(UserStock.class))).willAnswer(inv -> inv.getArgument(0));
        given(stockRepository.save(any(Stock.class))).willAnswer(inv -> withId(inv.getArgument(0), 10L));
    }

    @Test
    void 처음_보는_종목이면_외부에서_확인한_뒤_저장하고_관심_등록한다() {
        // given
        given(stockRepository.findByTicker("AAPL")).willReturn(Optional.empty());
        given(tickerProvider.findUsStock("AAPL")).willReturn(Optional.of(new TickerInfo("AAPL", "Apple Inc.")));
        given(userStockRepository.findById(any())).willReturn(Optional.empty());

        // when
        WatchedStockResponse result = service.add("AAPL");

        // then
        assertThat(result.ticker()).isEqualTo("AAPL");
        assertThat(result.name()).isEqualTo("Apple Inc.");
        then(stockRepository).should().save(any(Stock.class));
        then(userStockRepository).should().save(any(UserStock.class));
    }

    @Test
    void 이미_DB에_있는_종목이면_외부_API를_부르지_않는다() {
        given(stockRepository.findByTicker("AAPL")).willReturn(Optional.of(stock(10L, "AAPL")));
        given(userStockRepository.findById(any())).willReturn(Optional.empty());

        service.add("AAPL");

        then(tickerProvider).should(never()).findUsStock(any());
    }

    @Test
    void 티커는_앞뒤_공백을_지우고_대문자로_바꿔서_처리한다() {
        given(stockRepository.findByTicker("AAPL")).willReturn(Optional.of(stock(10L, "AAPL")));
        given(userStockRepository.findById(any())).willReturn(Optional.empty());

        WatchedStockResponse result = service.add("  aapl ");

        assertThat(result.ticker()).isEqualTo("AAPL");
    }

    @Test
    void 확인되지_않는_티커면_TickerNotFoundException() {
        given(stockRepository.findByTicker("APPLX")).willReturn(Optional.empty());
        given(tickerProvider.findUsStock("APPLX")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.add("APPLX"))
                .isInstanceOf(TickerNotFoundException.class);
        then(userStockRepository).should(never()).save(any());
    }

    @Test
    void 이미_관심_종목이면_AlreadyWatchingException() {
        Stock aapl = stock(10L, "AAPL");
        UserStock watching = new UserStock(user, aapl); // given(...) 안에서 만들면 Mockito가 헷갈린다. 먼저 만든다.
        given(stockRepository.findByTicker("AAPL")).willReturn(Optional.of(aapl));
        given(userStockRepository.findById(any())).willReturn(Optional.of(watching));

        assertThatThrownBy(() -> service.add("AAPL"))
                .isInstanceOf(AlreadyWatchingException.class);
    }

    @Test
    void 해제했던_종목을_다시_추가하면_같은_행을_되살린다() {
        Stock aapl = stock(10L, "AAPL");
        UserStock removed = new UserStock(user, aapl);
        removed.delete();
        given(stockRepository.findByTicker("AAPL")).willReturn(Optional.of(aapl));
        given(userStockRepository.findById(any())).willReturn(Optional.of(removed));

        service.add("AAPL");

        assertThat(removed.isDeleted()).isFalse();
        then(userStockRepository).should(never()).save(any()); // 새로 만들지 않는다
    }

    @Test
    void 일반_사용자가_최대_개수에_도달하면_WatchlistLimitExceededException() {
        given(userStockRepository.countActive(1L)).willReturn((long) MAX);

        assertThatThrownBy(() -> service.add("AAPL"))
                .isInstanceOf(WatchlistLimitExceededException.class);
        then(tickerProvider).should(never()).findUsStock(any()); // 제한에 걸리면 외부 API도 안 부른다
    }

    @Test
    void 관리자는_개수_제한이_없다() {
        User admin = userWith(1L, true);
        given(currentUserProvider.currentUser()).willReturn(admin);
        given(userStockRepository.countActive(1L)).willReturn(100L);
        given(stockRepository.findByTicker("AAPL")).willReturn(Optional.of(stock(10L, "AAPL")));
        given(userStockRepository.findById(any())).willReturn(Optional.empty());

        WatchedStockResponse result = service.add("AAPL");

        assertThat(result.ticker()).isEqualTo("AAPL");
    }

    @Test
    void 관심_종목을_해제하면_소프트_삭제된다() {
        Stock aapl = stock(10L, "AAPL");
        UserStock watching = new UserStock(user, aapl);
        given(stockRepository.findByTicker("AAPL")).willReturn(Optional.of(aapl));
        given(userStockRepository.findById(any())).willReturn(Optional.of(watching));

        service.remove("aapl");

        assertThat(watching.isDeleted()).isTrue();
    }

    @Test
    void 관심_종목이_아니면_해제할_때_NotWatchingException() {
        given(stockRepository.findByTicker("AAPL")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.remove("AAPL"))
                .isInstanceOf(NotWatchingException.class);
    }

    // ---- 테스트용 객체 만들기 ----

    private static User userWith(Long id, boolean admin) {
        User user = mock(User.class);
        given(user.getId()).willReturn(id);
        given(user.isAdmin()).willReturn(admin);
        return user;
    }

    private static Stock stock(Long id, String ticker) {
        return withId(new Stock(ticker, ticker + " Inc."), id);
    }

    /** id는 DB가 매기는 값이라 setter가 없다. 테스트에서만 강제로 넣는다. */
    private static Stock withId(Stock stock, Long id) {
        ReflectionTestUtils.setField(stock, "id", id);
        return stock;
    }
}
