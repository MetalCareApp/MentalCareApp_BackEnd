package com.remind.remind.repository.user;

import com.remind.remind.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 데이터베이스의 users 테이블에 접근하게 해주는 인터페이스입니다.
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 아이디(username)가 이미 DB에 존재하는지 확인하는 메서드 (중복 가입 방지)
    boolean existsByUsername(String username);

    // 아이디로 사용자 정보를 조회합니다.
    Optional<User> findByUsername(String username);
}
