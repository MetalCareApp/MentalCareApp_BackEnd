package com.remind.remind.controller.report;

import com.remind.remind.config.security.PrincipalDetails;
import com.remind.remind.dto.report.ReportCreateRequest;
import com.remind.remind.dto.report.ReportResponse;
import com.remind.remind.service.report.ReportCommandService;
import com.remind.remind.service.report.ReportQueryService;
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
@Tag(name = "AI 리포트", description = "AI 기반 심리 분석 리포트 관리 API")
public class ReportController {

    private final ReportCommandService reportCommandService;
    private final ReportQueryService reportQueryService;

    /**
     * AI 리포트 생성 (프론트 협의 경로: /ai/report)
     */
    @PostMapping("/ai/report")
    @Operation(summary = "AI 리포트 생성", description = "최근 일기 데이터를 바탕으로 AI 심리 분석 리포트를 생성합니다.")
    public ResponseEntity<ReportResponse> createReport(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody ReportCreateRequest request) {
        ReportResponse response = reportCommandService.createAiReport(principalDetails.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 내 리포트 목록 조회
     */
    @GetMapping("/reports")
    @Operation(summary = "내 리포트 목록 조회", description = "내가 생성한 모든 AI 리포트 목록을 최신순으로 조회합니다.")
    public ResponseEntity<List<ReportResponse>> getReports(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        List<ReportResponse> responses = reportQueryService.getReports(principalDetails.getUser().getId());
        return ResponseEntity.ok(responses);
    }

    /**
     * 리포트 상세 조회
     */
    @GetMapping("/reports/{id}")
    @Operation(summary = "리포트 상세 조회", description = "특정 AI 리포트의 상세 내용을 조회합니다.")
    public ResponseEntity<ReportResponse> getReport(@PathVariable Long id) {
        ReportResponse response = reportQueryService.getReport(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 리포트 삭제
     */
    @DeleteMapping("/reports/{id}")
    @Operation(summary = "리포트 삭제", description = "특정 AI 리포트를 삭제(Soft Delete)합니다.")
    public ResponseEntity<Void> deleteReport(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long id) {
        reportCommandService.deleteReport(id, principalDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
