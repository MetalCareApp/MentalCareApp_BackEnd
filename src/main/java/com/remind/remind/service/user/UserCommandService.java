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

import java.util.Map;
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
     * 회원 탈퇴 (Soft Delete + Email Obfuscation)
     */
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        
        user.withdraw();
        userRepository.save(user);
    }

    public TokenResponse loginOrSignup(String idToken) {
        Map<String, String> userInfo = userQueryService.verifyGoogleIdToken(idToken);
        String email = userInfo.get("email");
        String googleName = userInfo.get("name");

        // 유저가 존재하면 로그인 (탈퇴한 계정은 @Where에 의해 걸러짐), 없으면 신규 가입
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 신규 유저 생성 (구글 이름 사용)
                    return userRepository.save(User.builder()
                            .email(email)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .name(googleName)
                            .role(Role.USER)
                            .build());
                });

        // 토큰 발급
        String token = jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getRole().name());
        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }

    /*
    public TokenResponse signup(SignupRequest request) {
        Map<String, String> userInfo = userQueryService.verifyGoogleIdToken(request.getIdToken());
        String email = userInfo.get("email");
        String googleName = userInfo.get("name");

        if (userRepository.existsByEmail(email)) {
            throw new BaseException(ErrorCode.ALREADY_REGISTERED);
        }

        // 이름이 요청에 없으면 구글 이름 사용
        String name = request.getName();
        if (name == null || name.isBlank()) {
            name = googleName;
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .name(name)
                .role(Role.USER)
                .phone(request.getPhone())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .build();

        User savedUser = userRepository.save(user);

        String token = jwtTokenProvider.createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());
        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }
    */
}
