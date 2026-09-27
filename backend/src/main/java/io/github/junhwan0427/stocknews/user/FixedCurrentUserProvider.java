package io.github.junhwan0427.stocknews.user;

import io.github.junhwan0427.stocknews.common.config.StockNewsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 인증을 붙이기 전까지 쓰는 구현체. 설정에 적힌 이메일의 사용자(V4 마이그레이션의 관리자)를 항상 돌려준다. */
@Component
@RequiredArgsConstructor // final 필드를 받는 생성자를 Lombok이 만들어준다 → 스프링이 생성자 주입
public class FixedCurrentUserProvider implements CurrentUserProvider {

    private final UserRepository userRepository;
    private final StockNewsProperties properties;

    @Override
    public User currentUser() {
        String email = properties.fixedUser().email();
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalStateException(
                        "고정 사용자(" + email + ")가 DB에 없습니다. V4 마이그레이션과 stocknews.fixed-user.email 을 확인하세요."));
    }
}
