package com.remind.remind.repository.user;

import com.remind.remind.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import com.remind.remind.domain.user.Role;
import java.util.List;

import org.springframework.data.jpa.repository.Query;

// 데이터베이스의 users 테이블에 접근하게 해주는 인터페이스입니다.
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 이메일(email)이 이미 DB에 존재하는지 확인하는 메서드 (중복 가입 방지)
    boolean existsByEmail(String email);

    // 이메일로 사용자 정보를 조회합니다.
    Optional<User> findByEmail(String email);

    // 탈퇴한 사용자 포함 이메일로 조회 (네이티브 쿼리 사용)
    @Query(value = "SELECT * FROM users WHERE email = ?", nativeQuery = true)
    Optional<User> findByEmailWithDeleted(String email);

    // 이메일 포함 검색 (Role 필터링 포함)
    List<User> findByEmailContainingAndRole(String email, Role role);
}
