package io.github.junhwan0427.stocknews.stock;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * user_stocks 의 기본키 (user_id, stock_id) 두 컬럼을 묶은 객체. 이런 키를 복합키라고 한다.
 * JPA는 복합키를 비교할 때 equals/hashCode 를 쓰므로 반드시 만들어야 한다.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class UserStockId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "stock_id")
    private Long stockId;
}
