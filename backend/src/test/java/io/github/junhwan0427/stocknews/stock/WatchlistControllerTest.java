package io.github.junhwan0427.stocknews.stock;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.junhwan0427.stocknews.TestcontainersConfiguration;
import io.github.junhwan0427.stocknews.external.ticker.TickerInfo;
import io.github.junhwan0427.stocknews.external.ticker.TickerProvider;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관심 종목 API 통합 테스트. 진짜 PostgreSQL(Testcontainers) + Flyway 마이그레이션 + 스프링 전체를 띄운다.
 * 외부 API(Massive)만 가짜로 바꾼다(@MockitoBean). (설계서 B-6)
 * MockMvc: 서버를 실제로 띄우지 않고 HTTP 요청을 흉내 내서 컨트롤러를 호출하는 도구.
 * @Transactional: 테스트마다 DB 변경을 되돌려서, 테스트끼리 데이터가 섞이지 않게 한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class WatchlistControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TickerProvider tickerProvider;

    @BeforeEach
    void setUp() {
        given(tickerProvider.findUsStock("AAPL")).willReturn(Optional.of(new TickerInfo("AAPL", "Apple Inc.")));
        given(tickerProvider.findUsStock("APPLX")).willReturn(Optional.empty());
    }

    @Test
    void 관심_종목을_추가하면_201이고_목록에_보인다() throws Exception {
        addStock("aapl")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticker").value("AAPL"))
                .andExpect(jsonPath("$.name").value("Apple Inc."))
                .andExpect(jsonPath("$.notifyEnabled").value(true));

        mockMvc.perform(get("/me/stocks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ticker").value("AAPL"));
    }

    @Test
    void 같은_종목을_두번_추가하면_409와_RFC7807_에러() throws Exception {
        addStock("AAPL").andExpect(status().isCreated());

        addStock("AAPL")
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("이미 관심 종목"));
    }

    @Test
    void 확인되지_않는_티커면_400() throws Exception {
        addStock("APPLX")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("존재하지 않는 종목"));
    }

    @Test
    void 티커_형식이_틀리면_검증_단계에서_400() throws Exception {
        addStock("123")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("입력값 오류"))
                .andExpect(jsonPath("$.errors.ticker").value("티커 형식이 올바르지 않습니다. 예: AAPL, BRK.B"));
    }

    @Test
    void 해제하면_204이고_목록에서_빠진다() throws Exception {
        addStock("AAPL").andExpect(status().isCreated());

        mockMvc.perform(delete("/me/stocks/AAPL"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/me/stocks"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void 관심_종목이_아닌데_해제하면_404() throws Exception {
        mockMvc.perform(delete("/me/stocks/AAPL"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("관심 종목이 아님"));
    }

    private org.springframework.test.web.servlet.ResultActions addStock(String ticker) throws Exception {
        return mockMvc.perform(post("/me/stocks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ticker\": \"" + ticker + "\"}"));
    }
}
