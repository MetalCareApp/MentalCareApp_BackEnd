package com.remind.remind.controller.user;

import com.remind.remind.config.security.PrincipalDetails;
import com.remind.remind.domain.user.Role;
import com.remind.remind.domain.user.User;
import com.remind.remind.domain.user.Doctor;
import com.remind.remind.dto.user.LoginRequest;
import com.remind.remind.dto.user.SignupRequest;
import com.remind.remind.dto.user.TokenResponse;
import com.remind.remind.service.user.UserCommandService;
import com.remind.remind.service.user.UserQueryService;
import com.remind.remind.service.user.DoctorQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final DoctorQueryService doctorQueryService;

    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signup(@Valid @RequestBody SignupRequest request) {
        TokenResponse response = userCommandService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = userQueryService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }
        
        User user = principalDetails.getUser();
        Map<String, Object> response = new HashMap<>();
        response.put("nickname", user.getNickname());
        response.put("role", user.getRole().name());
        response.put("username", user.getUsername());

        if (user.getRole() == Role.DOCTOR) {
            doctorQueryService.findByUserId(user.getId()).ifPresent(doctor -> {
                Map<String, Object> hospitalInfo = new HashMap<>();
                hospitalInfo.put("name", doctor.getHospital().getName());
                hospitalInfo.put("address", doctor.getHospital().getAddress());
                hospitalInfo.put("phoneNumber", doctor.getHospital().getPhoneNumber());
                
                response.put("hospital", hospitalInfo);
                response.put("specialization", doctor.getSpecialization());
            });
        }
        
        return ResponseEntity.ok(response);
    }
}
