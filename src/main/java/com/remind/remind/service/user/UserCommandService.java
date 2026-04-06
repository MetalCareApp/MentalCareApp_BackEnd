package com.remind.remind.service.user;

import com.remind.remind.config.jwt.JwtTokenProvider;
import com.remind.remind.domain.user.Role;
import com.remind.remind.domain.user.User;
import com.remind.remind.dto.user.SignupRequest;
import com.remind.remind.dto.user.TokenResponse;
import com.remind.remind.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserQueryService userQueryService;

    /**
     * 구글 토큰과 추가 입력 정보를 받아 회원가입을 완료합니다.
     */
    public TokenResponse signup(SignupRequest request) {
        String email = userQueryService.verifyGoogleIdToken(request.getIdToken());

        if (userRepository.existsByUsername(email)) {
            throw new IllegalArgumentException("이미 가입된 사용자입니다.");
        }

        User user = User.builder()
                .username(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .nickname(request.getNickname())
                .role(Role.USER)
                .phone(request.getPhone())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .build();

        User savedUser = userRepository.save(user);

        String token = jwtTokenProvider.createToken(savedUser.getId(), savedUser.getUsername(), savedUser.getRole().name());
        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }
}
