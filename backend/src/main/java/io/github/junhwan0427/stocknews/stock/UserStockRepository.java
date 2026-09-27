package io.github.junhwan0427.stocknews.stock;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserStockRepository extends JpaRepository<UserStock, UserStockId> {

    /**
     * 사용자의 (해제 안 된) 관심 종목을 종목 정보까지 한 번에 가져온다.
     * join fetch: 관심 종목 N개의 종목 정보를 N번 따로 조회하는 문제(N+1)를 막는다.
     */
    @Query("""
            select us from UserStock us
              join fetch us.stock
             where us.user.id = :userId
               and us.deletedAt is null
             order by us.createdAt
            """)
    List<UserStock> findActiveWithStock(@Param("userId") Long userId);

    @Query("select count(us) from UserStock us where us.user.id = :userId and us.deletedAt is null")
    long countActive(@Param("userId") Long userId);
}
