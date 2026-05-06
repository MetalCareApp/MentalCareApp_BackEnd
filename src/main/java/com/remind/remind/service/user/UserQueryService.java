package com.remind.remind.service.user;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.remind.remind.config.jwt.JwtTokenProvider;
import com.remind.remind.domain.user.User;
import com.remind.remind.dto.user.LoginRequest;
import com.remind.remind.dto.user.TokenResponse;
import com.remind.remind.exception.BaseException;
import com.remind.remind.exception.ErrorCode;
import com.remind.remind.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${spring.security.oauth2.client.registration.google.client-id:#{null}}")
    private String googleClientId;

    public TokenResponse login(LoginRequest request) {
        String email = verifyGoogleIdToken(request.getIdToken());

        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), user.getRole().name());
        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }

    // 테스트용 및 구글 토큰 검증
    public String verifyGoogleIdToken(String idTokenString) {
        if (idTokenString == null || idTokenString.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_TOKEN);
        }

        // 개발 모드이거나, 토큰 형식이 JWT가 아닌 경우 (점 '.'이 2개 미만인 경우) 테스트용으로 간주
        if (isDevelopmentMode() || isProbablyMockToken(idTokenString)) {
            return idTokenString + "@example.com";
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return idToken.getPayload().getEmail();
            } else {
                throw new BaseException(ErrorCode.INVALID_TOKEN);
            }
        } catch (Exception e) {
            // 형식이 JWT여서 검증을 시도했으나 실패한 경우
            System.err.println("Real Token Verification Failed: " + e.getMessage());
            throw new BaseException(ErrorCode.INVALID_TOKEN);
        }
    }

    private boolean isDevelopmentMode() {
        boolean isRealConfig = googleClientId != null && googleClientId.contains(".apps.googleusercontent.com");
        return !isRealConfig;
    }

    private boolean isProbablyMockToken(String token) {
        // 실제 구글 ID 토큰(JWT)은 점(.)이 2개 포함된 'header.payload.signature' 구조입니다.
        // 점이 없거나 1개뿐이라면 테스트용 짧은 문자열(remind123 등)로 판단합니다.
        long dotCount = token.chars().filter(ch -> ch == '.').count();
        return dotCount < 2;
    }
}
