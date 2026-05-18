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

import com.remind.remind.domain.user.MatchStatus;
import com.remind.remind.repository.user.MatchRepository;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final DoctorQueryService doctorQueryService;
    private final MatchRepository matchRepository;

    /*
    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signup(@Valid @RequestBody SignupRequest request) {
        TokenResponse response = userCommandService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    */

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = userCommandService.loginOrSignup(request.getIdToken());
        return ResponseEntity.ok(response);
    }

    /**
     * 회원 탈퇴 (Soft Delete)
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        userCommandService.withdraw(principalDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> getMyInfo(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User user = principalDetails.getUser();
        UserMeResponse.UserMeResponseBuilder responseBuilder = UserMeResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .role(user.getRole().name())
                .email(user.getEmail());

        // 환자일 경우 매칭 정보 추가
        if (!user.isDoctor()) {
            Long matchId = matchRepository.findAllByPatientIdAndStatus(user.getId(), MatchStatus.ACCEPTED)
                    .stream()
                    .findFirst()
                    .map(match -> match.getId())
                    .orElse(null);
            responseBuilder.matchId(matchId);
        } else {
            // 의사일 경우 matchId는 null (builder 기본값) 및 상세 정보 추가
            doctorQueryService.findByUserId(user.getId()).ifPresent(doctor -> {
                UserMeResponse.HospitalInfoResponse hospital = UserMeResponse.HospitalInfoResponse.builder()
                        .name(doctor.getHospital().getName())
                        .address(doctor.getHospital().getAddress())
                        .phone(doctor.getHospital().getPhone())
                        .build();

                UserMeResponse.DoctorInfoResponse doctorInfo = UserMeResponse.DoctorInfoResponse.builder()
                        .hospital(hospital)
                        .patientCount(doctor.getPatientCount() != null ? doctor.getPatientCount() : 0)
                        .build();
                
                responseBuilder.doctorInfo(doctorInfo);
            });
        }
        
        return ResponseEntity.ok(responseBuilder.build());
    }
}
