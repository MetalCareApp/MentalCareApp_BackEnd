package com.remind.remind.controller.examination;

import com.remind.remind.config.security.PrincipalDetails;
import com.remind.remind.dto.examination.ExaminationCreateRequest;
import com.remind.remind.dto.examination.ExaminationResponse;
import com.remind.remind.service.examination.ExaminationCommandService;
import com.remind.remind.service.examination.ExaminationQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/examinations")
@Tag(name = "Examination", description = "심리 검사 관련 API")
public class ExaminationController {

    private final ExaminationCommandService examinationCommandService;
    private final ExaminationQueryService examinationQueryService;

    @GetMapping
    @Operation(summary = "내 검사 이력 조회", description = "로그인한 사용자의 모든 심리 검사 이력을 최신순으로 조회합니다.")
    public ResponseEntity<List<ExaminationResponse>> getMyExaminations(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        List<ExaminationResponse> response = examinationQueryService.getMyExaminations(principalDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/phq-9")
    @Operation(summary = "PHQ-9 (우울증) 검사 생성", description = "우울증 자가진단 검사를 생성합니다. (9문항, 각 0~3점)")
    public ResponseEntity<ExaminationResponse> createPHQ9(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody ExaminationCreateRequest request) {
        
        ExaminationResponse response = examinationCommandService.createPHQ9(principalDetails.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/gad-7")
    @Operation(summary = "GAD-7 (불안장애) 검사 생성", description = "불안장애 자가진단 검사를 생성합니다. (7문항, 각 0~3점)")
    public ResponseEntity<ExaminationResponse> createGAD7(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody ExaminationCreateRequest request) {
        
        ExaminationResponse response = examinationCommandService.createGAD7(principalDetails.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/stress")
    @Operation(summary = "스트레스 검사 생성", description = "스트레스 지수 측정 검사를 생성합니다. (10문항, 각 0~4점)")
    public ResponseEntity<ExaminationResponse> createStress(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody ExaminationCreateRequest request) {
        
        ExaminationResponse response = examinationCommandService.createStress(principalDetails.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/bipolar")
    @Operation(summary = "조울증 검사 생성", description = "조울증 가능성 체크 검사를 생성합니다. (13문항, 각 0~1점)")
    public ResponseEntity<ExaminationResponse> createBipolar(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody ExaminationCreateRequest request) {
        
        ExaminationResponse response = examinationCommandService.createBipolar(principalDetails.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/adhd")
    @Operation(summary = "ADHD 검사 생성", description = "성인 ADHD 자가진단 검사를 생성합니다. (18문항, 각 0~3점)")
    public ResponseEntity<ExaminationResponse> createADHD(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody ExaminationCreateRequest request) {
        
        ExaminationResponse response = examinationCommandService.createADHD(principalDetails.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
