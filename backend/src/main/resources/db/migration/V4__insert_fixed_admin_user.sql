-- 인증을 붙이기 전까지 모든 요청을 처리할 고정 사용자(관리자).
-- application.yml 의 stocknews.fixed-user.email 과 같아야 한다.
INSERT INTO users (email, nickname, role)
VALUES ('admin@stocknews.local', 'admin', 'ADMIN');
