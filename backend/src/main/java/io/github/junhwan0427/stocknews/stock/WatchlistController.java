package io.github.junhwan0427.stocknews.stock;

import io.github.junhwan0427.stocknews.stock.dto.AddStockRequest;
import io.github.junhwan0427.stocknews.stock.dto.WatchedStockResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 내 관심 종목 API. /me 는 "지금 요청한 사용자"를 뜻한다. 누구인지는 서버(CurrentUserProvider)가 정한다.
 * 컨트롤러는 요청을 받아 서비스에 넘기기만 한다. 에러 처리는 GlobalExceptionHandler 가 한다.
 */
@RestController
@RequestMapping("/me/stocks")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    /** 내 관심 종목 목록. 200 OK */
    @GetMapping
    public List<WatchedStockResponse> list() {
        return watchlistService.getMyStocks();
    }

    /** 관심 종목 추가. 201 Created */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WatchedStockResponse add(@Valid @RequestBody AddStockRequest request) {
        return watchlistService.add(request.ticker());
    }

    /** 관심 종목 해제. 204 No Content (돌려줄 본문 없음) */
    @DeleteMapping("/{ticker}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable String ticker) {
        watchlistService.remove(ticker);
    }
}
