package com.remind.remind.controller.diary;

import com.remind.remind.config.security.PrincipalDetails;
import com.remind.remind.dto.diary.DiaryCreateRequest;
import com.remind.remind.dto.diary.DiaryResponse;
import com.remind.remind.service.diary.DiaryCommandService;
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
@RequestMapping("/diaries")
public class DiaryController {

    private final DiaryCommandService diaryCommandService;

    @PostMapping
    public ResponseEntity<DiaryResponse> createDiary(
            @Valid @RequestBody DiaryCreateRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        DiaryResponse response = diaryCommandService.createDiary(request, principalDetails.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
