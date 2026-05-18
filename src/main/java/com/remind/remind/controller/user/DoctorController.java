package com.remind.remind.controller.user;

import com.remind.remind.config.security.PrincipalDetails;
import com.remind.remind.dto.user.DoctorSignupRequest;
import com.remind.remind.dto.user.TokenResponse;
import com.remind.remind.service.user.DoctorCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorCommandService doctorCommandService;

    /**
     * 의사 회원가입 신청 (수동 승인 대기) - 기존 JSON 방식 유지
     */
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @Valid @RequestBody DoctorSignupRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        doctorCommandService.signup(principalDetails.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 의사 회원가입 신청 v2 (S3 사진 업로드 포함) - 프론트엔드 대응용 별도 API
     */
    @PostMapping(value = "/signup/v2", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> signupV2(
            @RequestPart("request") @Valid DoctorSignupRequest request,
            @RequestPart(value = "certificationImage", required = false) MultipartFile certificationImage,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        doctorCommandService.signupV2(principalDetails.getUser().getId(), request, certificationImage);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
