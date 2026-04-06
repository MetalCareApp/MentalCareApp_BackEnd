package com.remind.remind.controller.user;

import com.remind.remind.config.security.PrincipalDetails;
import com.remind.remind.domain.user.Role;
import com.remind.remind.domain.user.User;
import com.remind.remind.dto.user.LoginRequest;
import com.remind.remind.dto.user.SignupRequest;
import com.remind.remind.dto.user.TokenResponse;
import com.remind.remind.dto.user.UserMeResponse;
import com.remind.remind.service.user.UserCommandService;
import com.remind.remind.service.user.UserQueryService;
import com.remind.remind.service.user.DoctorQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<UserMeResponse> getMyInfo(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User user = principalDetails.getUser();
        UserMeResponse.UserMeResponseBuilder responseBuilder = UserMeResponse.builder()
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .username(user.getUsername());

        // 의사일 경우 상세 정보 추가
        if (user.isDoctor()) {
            doctorQueryService.findByUserId(user.getId()).ifPresent(doctor -> {
                UserMeResponse.HospitalInfoResponse hospital = UserMeResponse.HospitalInfoResponse.builder()
                        .name(doctor.getHospital().getName())
                        .address(doctor.getHospital().getAddress())
                        .phoneNumber(doctor.getHospital().getPhoneNumber())
                        .build();

                UserMeResponse.DoctorInfoResponse doctorInfo = UserMeResponse.DoctorInfoResponse.builder()
                        .specialization(doctor.getSpecialization())
                        .hospital(hospital)
                        .patientCount(doctor.getPatients() != null ? doctor.getPatients().size() : 0)
                        .build();
                
                responseBuilder.doctorInfo(doctorInfo);
            });
        }
        
        return ResponseEntity.ok(responseBuilder.build());
    }
}
