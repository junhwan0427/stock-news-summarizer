package io.github.junhwan0427.stocknews.user;

/**
 * "지금 요청한 사용자가 누구인지" 알려주는 포트.
 * 지금은 FixedCurrentUserProvider(고정 사용자), 인증을 붙이면 로그인 정보를 읽는 구현체로 교체한다.
 * 이 인터페이스만 쓰는 나머지 코드는 교체해도 바뀌지 않는다.
 */
public interface CurrentUserProvider {

    User currentUser();
}
