package com.remind.remind.controller.examination;

import com.remind.remind.config.security.PrincipalDetails;
import com.remind.remind.dto.examination.ExaminationCreateRequest;
import com.remind.remind.dto.examination.ExaminationResponse;
import com.remind.remind.service.examination.ExaminationCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/examinations")
public class ExaminationController {

    private final ExaminationCommandService examinationCommandService;

    @PostMapping("/phq-9")
    public ResponseEntity<ExaminationResponse> createPHQ9(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody ExaminationCreateRequest request) {
        
        ExaminationResponse response = examinationCommandService.createPHQ9(principalDetails.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/gad-7")
    public ResponseEntity<ExaminationResponse> createGAD7(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody ExaminationCreateRequest request) {
        
        ExaminationResponse response = examinationCommandService.createGAD7(principalDetails.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
