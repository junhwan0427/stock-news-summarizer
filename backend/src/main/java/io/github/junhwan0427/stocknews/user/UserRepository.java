package io.github.junhwan0427.stocknews.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // 메서드 이름만으로 쿼리가 만들어진다: where email = ? and deleted_at is null
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
}
