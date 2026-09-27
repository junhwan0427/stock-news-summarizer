package io.github.junhwan0427.stocknews.stock;

import io.github.junhwan0427.stocknews.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * user_stocks 테이블: 사용자의 관심 종목.
 * 해제는 행을 지우지 않고 deletedAt 을 채운다(소프트 삭제). 다시 추가하면 같은 행을 되살린다.
 */
@Entity
@Table(name = "user_stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStock {

    @EmbeddedId
    private UserStockId id;

    // @MapsId: 이 연관관계의 id를 복합키의 userId 칸에 채운다. LAZY: 실제로 쓸 때까지 users 조회를 미룬다.
    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("stockId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    /** 이 종목 요약을 알림으로 받을지. */
    @Column(nullable = false)
    private boolean notify;

    /** 관심 등록 시각. 되살리면 그 시각으로 바뀐다. */
    @Column(nullable = false)
    private Instant createdAt;

    private Instant deletedAt;

    public UserStock(User user, Stock stock) {
        this.id = new UserStockId(user.getId(), stock.getId());
        this.user = user;
        this.stock = stock;
        this.notify = true;
        this.createdAt = Instant.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void delete() {
        this.deletedAt = Instant.now();
    }

    public void restore() {
        this.deletedAt = null;
        this.createdAt = Instant.now();
    }
}
