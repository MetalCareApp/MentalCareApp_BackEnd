package com.remind.remind.service.user;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.remind.remind.config.jwt.JwtTokenProvider;
import com.remind.remind.domain.user.Role;
import com.remind.remind.domain.user.User;
import com.remind.remind.dto.user.LoginRequest;
import com.remind.remind.dto.user.SignupRequest;
import com.remind.remind.dto.user.TokenResponse;
import com.remind.remind.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    /**
     * 구글 로그인을 시도합니다. 가입된 유저면 토큰을 주고, 아니면 에러(404)를 던져 가입 화면으로 유도합니다.
     */
    public TokenResponse login(LoginRequest request) {
        String email = verifyGoogleIdToken(request.getIdToken());

        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND_USER")); // "NOT_FOUND_USER"라는 메시지로 구분

        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), user.getRole().name());
        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }

    /**
     * 구글 토큰과 추가 입력 정보를 받아 회원가입을 완료합니다. (기본 권한: USER)
     */
    @Transactional
    public TokenResponse signup(SignupRequest request) {
        String email = verifyGoogleIdToken(request.getIdToken());

        if (userRepository.existsByUsername(email)) {
            throw new IllegalArgumentException("이미 가입된 사용자입니다.");
        }

        User user = User.builder()
                .username(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // 소셜 로그인은 임의의 비밀번호 설정
                .nickname(request.getNickname())
                .role(Role.USER) // 정책에 따라 무조건 '환자'로 시작
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

    /**
     * 구글 ID 토큰을 검증하여 이메일을 반환합니다.
     */
    private String verifyGoogleIdToken(String idTokenString) {
        // [테스트용] idToken이 "test-token"이면 검증 없이 테스트용 이메일 반환
        if ("test-token".equals(idTokenString)) {
            return "test@example.com";
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return idToken.getPayload().getEmail();
            } else {
                throw new IllegalArgumentException("유효하지 않은 구글 토큰입니다.");
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalArgumentException("구글 토큰 검증 중 오류가 발생했습니다.");
        }
    }
}
