package com.remind.remind.controller.diary;

import com.remind.remind.config.security.PrincipalDetails;
import com.remind.remind.dto.diary.DiaryCreateRequest;
import com.remind.remind.dto.diary.DiaryResponse;
import com.remind.remind.service.diary.DiaryCommandService;
import com.remind.remind.service.diary.DiaryQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diaries")
public class DiaryController {

    private final DiaryCommandService diaryCommandService;
    private final DiaryQueryService diaryQueryService;

    @PostMapping
    public ResponseEntity<DiaryResponse> createDiary(
            @Valid @RequestBody DiaryCreateRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        DiaryResponse response = diaryCommandService.createDiary(request, principalDetails.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<DiaryResponse> getDiary(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        DiaryResponse response = diaryQueryService.getDiary(id, principalDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/months/{yyyymm}")
    public ResponseEntity<List<DiaryResponse>> getMonthlyDiaries(
            @PathVariable String yyyymm,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        List<DiaryResponse> response = diaryQueryService.getMonthlyDiaries(yyyymm, principalDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }
}
