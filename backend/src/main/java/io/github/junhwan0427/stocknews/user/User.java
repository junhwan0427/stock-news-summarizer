package io.github.junhwan0427.stocknews.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** users 테이블. 테이블 구조는 Flyway(V1)가 만들고, 이 클래스는 그 모양과 맞아야 한다(ddl-auto=validate). */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA가 쓰는 기본 생성자. 밖에서 빈 객체를 못 만들게 protected.
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING) // DB에 0,1 숫자가 아니라 'USER','ADMIN' 글자로 저장
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, updatable = false, insertable = false) // DB 기본값 now()가 채운다
    private Instant createdAt;

    private Instant deletedAt;

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
