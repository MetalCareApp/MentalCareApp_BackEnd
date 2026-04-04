package com.remind.remind.controller.user;

import com.remind.remind.config.security.PrincipalDetails;
import com.remind.remind.domain.user.User;
import com.remind.remind.dto.user.LoginRequest;
import com.remind.remind.dto.user.SignupRequest;
import com.remind.remind.dto.user.TokenResponse;
import com.remind.remind.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 API
     * 구글 토큰과 추가 정보를 받아 회원가입을 완료하고 토큰을 반환합니다.
     */
    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signup(@Valid @RequestBody SignupRequest request) {
        TokenResponse response = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 로그인 API
     * 가입된 사용자면 토큰(200 OK)을 반환하고, 아니면 404(Not Found)를 반환하여 회원가입 화면으로 유도합니다.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            TokenResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            if ("NOT_FOUND_USER".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("가입되지 않은 사용자입니다. 회원가입 화면으로 이동하세요.");
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * 내 정보 조회 API (토큰 테스트용)
     * 현재 로그인한 사용자의 닉네임과 권한(의사 여부)을 반환합니다.
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }
        
        User user = principalDetails.getUser();
        return ResponseEntity.ok(Map.of(
                "nickname", user.getNickname(),
                "role", user.getRole().name(),
                "username", user.getUsername()
        ));
    }
}
