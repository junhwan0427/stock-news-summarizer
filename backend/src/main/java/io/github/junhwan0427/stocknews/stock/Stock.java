package io.github.junhwan0427.stocknews.stock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** stocks 테이블: 시스템이 아는 종목. 사용자 수와 무관하게 티커당 한 행. */
@Entity
@Table(name = "stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    private static final int NAME_MAX_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String ticker;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    /** 시스템이 매일 수집·요약할지. 관심 사용자가 0명이 되면 채점 작업이 끈다. */
    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false, insertable = false)
    private Instant createdAt;

    private Instant deletedAt;

    public Stock(String ticker, String name) {
        this.ticker = ticker;
        // 회사명이 컬럼 길이보다 길면 저장이 실패하므로 잘라서 넣는다.
        this.name = name.length() > NAME_MAX_LENGTH ? name.substring(0, NAME_MAX_LENGTH) : name;
        this.active = true;
    }

    /** 누군가 다시 관심 종목으로 추가하면 수집을 다시 켠다. */
    public void activate() {
        this.active = true;
    }
}
