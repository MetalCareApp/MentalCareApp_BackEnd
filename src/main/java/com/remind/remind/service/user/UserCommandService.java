package com.remind.remind.service.user;

import com.remind.remind.config.jwt.JwtTokenProvider;
import com.remind.remind.domain.user.Role;
import com.remind.remind.domain.user.User;
import com.remind.remind.dto.user.SignupRequest;
import com.remind.remind.dto.user.TokenResponse;
import com.remind.remind.exception.BaseException;
import com.remind.remind.exception.ErrorCode;
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

    public TokenResponse loginOrSignup(String idToken) {
        String email = userQueryService.verifyGoogleIdToken(idToken);

        // 유저가 존재하면 로그인, 없으면 회원가입 진행
        User user = userRepository.findByUsername(email)
                .orElseGet(() -> {
                    // 신규 유저 생성 (닉네임 기본값: 이메일 앞자리)
                    String defaultNickname = email.split("@")[0];
                    return userRepository.save(User.builder()
                            .username(email)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .nickname(defaultNickname)
                            .role(Role.USER)
                            .build());
                });

        // 토큰 발급
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), user.getRole().name());
        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }

    public TokenResponse signup(SignupRequest request) {
        String email = userQueryService.verifyGoogleIdToken(request.getIdToken());

        if (userRepository.existsByUsername(email)) {
            throw new BaseException(ErrorCode.ALREADY_REGISTERED);
        }

        // 닉네임이 없을 경우 이메일 앞부분을 기본값으로 사용
        String nickname = request.getNickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = email.split("@")[0];
        }

        User user = User.builder()
                .username(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .nickname(nickname)
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
